package com.geulkkoli.web.user;

import com.geulkkoli.application.user.AuthUser;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.UserService;
import com.geulkkoli.web.user.edit.EditForm;
import com.geulkkoli.web.user.edit.EditPasswordForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    public static final String LOGIN_FORM = "user/loginForm";
    public static final String JOIN_FORM = "user/joinForm";
    public static final String EDIT_FORM = "user/edit/editForm";
    public static final String EDIT_PASSWORD_FORM = "user/edit/editPassword";
    public static final String REDIRECT_INDEX = "redirect:/";
    private final UserService userService;

    @GetMapping("/loginPage")
    public String loginForm(@ModelAttribute("loginForm") LoginFormDto form) {
        return LOGIN_FORM;
    }

    @PostMapping("/loginPage")
    public String loginError(@ModelAttribute("loginForm") LoginFormDto form) {
        return LOGIN_FORM;
    }

    //join

    //join
    @GetMapping("/join")
    public String joinForm(@ModelAttribute("joinForm") JoinFormDto form) {
        return JOIN_FORM;
    }

    @PostMapping("/join")
    public String userJoin(@Validated @ModelAttribute("joinForm") JoinFormDto form, BindingResult bindingResult, Model model) {
        log.info("join Method={}", this);
        if (bindingResult.hasErrors()) {
            return JOIN_FORM;
        }

        if (userService.isEmailDuplicate(form.getEmail())) {
            bindingResult.rejectValue("email", "Duple.joinForm.email");
            return JOIN_FORM;
        }

        if (userService.isNickNameDuplicate(form.getNickName())) {
            bindingResult.rejectValue("nickName", "Duple.nickName");
            return JOIN_FORM;

        }

        if (userService.isPhoneNoDuplicate(form.getPhoneNo())) {
            bindingResult.rejectValue("phoneNo", "Duple.phoneNo");
            return JOIN_FORM;

        }

        // 중복 검사라기보다는 비밀번호 확인에 가까운 것 같아서 에러코드명 변경
        if (!form.getPassword().equals(form.getVerifyPassword())) {
            bindingResult.rejectValue("verifyPassword", "Check.verifyPassword");
            return JOIN_FORM;

        }

        if (!bindingResult.hasErrors()) {
            userService.join(form);
        }

        log.info("joinModel = {}", model);
        log.info("joinForm = {}", form);

        return REDIRECT_INDEX;
    }


    @GetMapping("/edit")
    public String editForm(@ModelAttribute("editForm") EditForm editForm, @AuthenticationPrincipal AuthUser authUser, Model model) {
        editForm.editForm(authUser.getUserRealName(), authUser.getNickName(), authUser.getPhoneNo(), authUser.getGender());
        model.addAttribute("editForm", editForm);
        return EDIT_FORM;
    }

    /*
    * TODO: 수정된 회원정보가 세션에 저장되어 있지 않음 추후에 바꾸겠다
    * authUser가 기존의 세션 저장 방식을 대체한다
    * */
    @PostMapping("/edit")
    public String editForm(@Validated @ModelAttribute("editForm") EditForm editForm, BindingResult bindingResult, @AuthenticationPrincipal AuthUser authUser){

        if (bindingResult.hasErrors()) {
            return EDIT_FORM;
        }

        // 닉네임 중복 검사 && 본인의 기존 닉네임과 일치해도 중복이라고 안 뜨게
        if (userService.isNickNameDuplicate(editForm.getNickName()) && !editForm.getNickName().equals(authUser.getNickName())) {
            bindingResult.rejectValue("nickName", "Duple.nickName");
            return EDIT_FORM;
        }

        if (userService.isPhoneNoDuplicate(editForm.getPhoneNo()) && !editForm.getPhoneNo().equals(authUser.getPhoneNo())) {
            bindingResult.rejectValue("phoneNo", "Duple.phoneNo");
            return EDIT_FORM;
        }

        if (!bindingResult.hasErrors()) {
            Optional<User> updateUser = userService.update(authUser.getUserId(), editForm);
        }

        return "redirect:/edit";
    }

    @GetMapping("/editPassword")
    public String editPasswordForm(@ModelAttribute("editPasswordForm") EditPasswordForm form) {
        return EDIT_PASSWORD_FORM;
    }

    @PostMapping("/editPassword")
    public String editPassword(@Validated @ModelAttribute("editPasswordForm") EditPasswordForm form, BindingResult bindingResult, @AuthenticationPrincipal AuthUser authUser, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return EDIT_PASSWORD_FORM;
        }

        if (!userService.isPasswordVerification(new User(), form)) {
            bindingResult.rejectValue("password", "Check.password");
            return EDIT_PASSWORD_FORM;
        }

        if (!form.getNewPassword().equals(form.getVerifyPassword())) {
            bindingResult.rejectValue("verifyPassword", "Check.verifyPassword");
            return EDIT_PASSWORD_FORM;
        }

        if (!bindingResult.hasErrors()) {
            userService.updatePassword(authUser.getUserId(), form);
            redirectAttributes.addAttribute("status", true);
            log.info("editPasswordForm = {}", form);
        }

        return "redirect:/edit";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return REDIRECT_INDEX;
    }

    @PostMapping("/memberDelete")
    public String memberDelete(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            userService.delete((User)session.getAttribute(SessionConst.LOGIN_USER));
            session.invalidate();
        }
        return "redirect:/";
    }
}
