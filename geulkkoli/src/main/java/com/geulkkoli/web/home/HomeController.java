package com.geulkkoli.web.home;

import com.geulkkoli.application.EmailService;
import com.geulkkoli.application.user.service.PasswordService;
import com.geulkkoli.domain.posthashtag.service.PostHashTagService;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.UserFindService;
import com.geulkkoli.domain.user.service.UserService;
import com.geulkkoli.web.user.ResponseMessage;
import com.geulkkoli.web.user.dto.EmailCheckForJoinDto;
import com.geulkkoli.web.user.dto.JoinFormDto;
import com.geulkkoli.web.user.dto.LoginFormDto;
import com.geulkkoli.web.user.dto.find.FindEmailFormDto;
import com.geulkkoli.web.user.dto.find.FindPasswordFormDto;
import com.geulkkoli.web.user.dto.find.FoundEmailFormDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
public class HomeController {
    private  final String FIND_EMAIL_FORM = "user/find/findEmailForm";
    private  final String FOUND_EMAIL_FORM = "user/find/foundEmailForm";
    private  final String FIND_PASSWORD_FORM = "user/find/findPasswordForm";
    private  final String TEMP_PASSWORD_FORM = "user/find/tempPasswordForm";
    private  final String JOIN_FORM = "user/joinForm";
    public static final String REDIRECT_INDEX = "redirect:/";


    private final PostHashTagService postHashTagService;
    private final EmailService emailService;
    private final UserFindService userFindService;
    private final UserService userService;
    private final PasswordService passwordService;

    @GetMapping
    public String home(@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model,
                       @RequestParam(defaultValue = "해시태그") String searchType,
                       @RequestParam(defaultValue = "일반") String searchWords) {
        model.addAttribute("list", postHashTagService.searchPostsListByHashTag(pageable, searchType, searchWords).toList());
        model.addAttribute("notificationList", postHashTagService.searchPostsListByHashTag(pageable, searchType, searchWords+"#공지글").toList());
        log.info("now : {}", LocalDate.now());
        model.addAttribute("todayTopic", postHashTagService.showTodayTopic(LocalDate.now()));
        return "/home";
    }

    @GetMapping("/loginPage")
    public String loginForm(@ModelAttribute("loginForm") LoginFormDto form) {

        return "user/loginForm";
    }

    @PostMapping("/loginPage")
    public String processLoginForm(@ModelAttribute("loginForm") LoginFormDto form) {

        return "user/loginForm"; // 실패 메시지를 포함한 GET 요청으로 리다이렉트
    }

    @GetMapping("/findEmail")
    public String findEmailForm(@ModelAttribute("findEmailForm") FindEmailFormDto form) {
        return FIND_EMAIL_FORM;
    }

    @PostMapping("/findEmail")
    public String userFindEmail(@Validated @ModelAttribute("findEmailForm") FindEmailFormDto form, BindingResult bindingResult, Model model) {

        Optional<User> user = userFindService.findByUserNameAndPhoneNo(form.getUserName(), form.getPhoneNo());

        if (user.isEmpty()) {
            bindingResult.addError(new ObjectError("empty", "Check.findContent"));
        }

        if (!bindingResult.hasErrors()) {
            FoundEmailFormDto foundEmail = new FoundEmailFormDto(user.get().getEmail());
            model.addAttribute("email", foundEmail.getEmail());
            return FOUND_EMAIL_FORM;

        } else {
            return FIND_EMAIL_FORM;
        }
    }

    @GetMapping("/foundEmail")
    public String foundEmailForm(@ModelAttribute("foundEmailForm") FoundEmailFormDto form) {
        return FOUND_EMAIL_FORM;
    }

    @GetMapping("/findPassword")
    public String findPasswordForm(@ModelAttribute("findPasswordForm") FindPasswordFormDto form) {
        return FIND_PASSWORD_FORM;
    }

    @PostMapping("/findPassword")
    public String userFindPassword(@Validated @ModelAttribute("findPasswordForm") FindPasswordFormDto form, BindingResult bindingResult, HttpServletRequest request) {

        Optional<User> user = userFindService.findByEmailAndUserNameAndPhoneNo(form.getEmail(), form.getUserName(), form.getPhoneNo());

        if (user.isEmpty()) {
            bindingResult.addError(new ObjectError("empty", "Check.findContent"));
        }

        if (!bindingResult.hasErrors()) {
            request.getSession().setAttribute("email", user.get().getEmail());
            return "forward:/tempPassword"; // post로 감
        } else {
            return FIND_PASSWORD_FORM;
        }
    }

    @PostMapping("/tempPassword")
    public String tempPasswordForm() {
        return TEMP_PASSWORD_FORM;
    }

    @GetMapping("/tempPassword")
    public String userTempPassword(HttpServletRequest request, Model model) {
        String email = (String) request.getSession().getAttribute("email");
        Optional<User> user = userFindService.findByEmail(email);

        int length = passwordService.setLength(8, 20);
        String tempPassword = passwordService.createTempPassword(length);

        passwordService.updatePassword(user.get().getUserId(), tempPassword);
        emailService.sendTempPasswordEmail(email, tempPassword);
        log.info("email 발송");

        model.addAttribute("waitMailMessage", true);
        return TEMP_PASSWORD_FORM;
    }

    //join
    @GetMapping("/join")
    public String joinForm(@ModelAttribute("joinForm") JoinFormDto form) {
        return JOIN_FORM;
    }

    @PostMapping("/join")
    public String userJoin(@Validated @ModelAttribute("joinForm") JoinFormDto form, BindingResult bindingResult, HttpServletRequest request) {
        if (userService.isNickNameDuplicate(form.getNickName())) {
            bindingResult.rejectValue("nickName", "Duple.nickName");
        }

        if (userService.isPhoneNoDuplicate(form.getPhoneNo())) {
            bindingResult.rejectValue("phoneNo", "Duple.phoneNo");
        }

        String authenticationEmail = (String) request.getSession().getAttribute("authenticationEmail");
        String authenticationNumber = (String) request.getSession().getAttribute("authenticationNumber");
        if (authenticationEmail.isEmpty() || authenticationNumber.isEmpty()) {
            bindingResult.rejectValue("email", "Authentication.email");
        }

        // 인증된 이메일 수정 후 인증 안 된 상태로 가입 시도할 경우
        if (!form.getEmail().equals(authenticationEmail)) {
            bindingResult.rejectValue("email", "Authentication.email");
        }

        if (!form.getPassword().equals(form.getVerifyPassword())) {
            bindingResult.rejectValue("verifyPassword", "Check.verifyPassword");
        }

        if (!bindingResult.hasErrors()) {
            userService.signUp(form);

            return REDIRECT_INDEX;
        } else {
            return JOIN_FORM;
        }
    }

    @PostMapping("/checkEmail")
    @ResponseBody
    public ResponseMessage checkEmail(@RequestBody EmailCheckForJoinDto form, HttpServletRequest request) {


        if (form.getEmail().isEmpty()) {
            return ResponseMessage.NULL_OR_BLANK_EMAIL;
        }
        if (userService.isEmailDuplicate(form.getEmail())) {
            return ResponseMessage.EMAIL_DUPLICATION;
        }
        int length = 6;
        String authenticationNumber = passwordService.authenticationNumber(length);
        request.getSession().setAttribute("authenticationNumber", authenticationNumber);
        emailService.sendAuthenticationNumberEmail(form.getEmail(), authenticationNumber);
        log.info("email 발송");

        return ResponseMessage.SEND_AUTHENTICATION_NUMBER_SUCCESS;
    }

    @PostMapping("/checkAuthenticationNumber")
    @ResponseBody
    public String checkAuthenticationNumber(@RequestBody EmailCheckForJoinDto form, HttpServletRequest request) {

        String authenticationNumber = (String) request.getSession().getAttribute("authenticationNumber");
        String responseMessage;

        if (!form.getAuthenticationNumber().trim().equals(authenticationNumber)) {
            responseMessage = "wrong";
        } else {
            request.getSession().setAttribute("authenticationEmail", form.getEmail());
            responseMessage = "right";
        }

        return responseMessage;
    }
}

