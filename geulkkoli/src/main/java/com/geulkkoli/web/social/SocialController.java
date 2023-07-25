package com.geulkkoli.web.social;

import com.geulkkoli.application.security.AccountStatus;
import com.geulkkoli.application.user.CustomAuthenticationPrinciple;
import com.geulkkoli.application.user.UserModelDto;
import com.geulkkoli.domain.social.service.SocialInfoService;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.UserService;
import com.geulkkoli.web.social.util.SocialSignUpValueEncryptoDecryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/social")
@Slf4j
@RequiredArgsConstructor
public class SocialController {
    private static final String SIGN_UP_VIEW_NAME = "social/oauth2/signup";
    private static final String HOME = "/home";
    private final UserService userService;

    private final SocialInfoService socialService;

    private final SocialSignUpValueEncryptoDecryptor socialSignUpValueEncryptoDecryptor;

    /**
     * @param authUser     각 인증 서버 (구글, 카카오, 네이버)에서 성공적으로 정보를 받아 인증이 완료되었지만 우리 서비스에 가입되지 않은 회원의 경우
     *                     추가 기입 이후 회원 가입 완료하기 위해 authUser 객체의 정보를 가져와 SocialSignUpDto 객체에 담아 회원 가입 페이지로 이동시킨다.
     *                     SecurityContextHolder.clearContext(); 인증 객체 customAuthenticationPrinciple을 제거하가 위해 쓴다.
     * @param modelAndView 어떤 view로 갈지 지정하고 model에 socialSignUpDto 객체를 담아 보내기 위해 사용한다.
     * @return modelAndView
     */
    @GetMapping("/oauth2/signup")
    public ModelAndView moveSignUpPage(@AuthenticationPrincipal CustomAuthenticationPrinciple authUser, ModelAndView modelAndView) {
        log.info("소셜회원가입");
        SocialSignUpDto socialSignUpDto = SocialSignUpDto.builder()
                .email(socialSignUpValueEncryptoDecryptor.encryptValue(authUser.getUsername()))
                .nickName(authUser.getNickName())
                .phoneNo(authUser.getPhoneNo())
                .verifyPassword(socialSignUpValueEncryptoDecryptor.encryptValue(authUser.getPassword()))
                .gender(authUser.getGender())
                .userName(socialSignUpValueEncryptoDecryptor.encryptValue(authUser.getUserRealName()))
                .password(socialSignUpValueEncryptoDecryptor.encryptValue(authUser.getPassword()))
                .authorizationServerId(socialSignUpValueEncryptoDecryptor.encryptValue(authUser.getUserId()))
                .clientregistrationName(socialSignUpValueEncryptoDecryptor.encryptValue(authUser.getSocialType().getValue()))
                .build();
        SecurityContextHolder.clearContext();

        modelAndView.addObject("signUpDto", socialSignUpDto);
        modelAndView.setViewName(SIGN_UP_VIEW_NAME);
        return modelAndView;
    }

    @PostMapping("/oauth2/signup")
    public ModelAndView signUp(@ModelAttribute("signUpDto") SocialSignUpDto signUpDtoUpDto, BindingResult bindingResult, ModelAndView modelAndView) {
        log.info("소셜 로그인 회원의 회원 정보를 보낸다.");
        modelAndView.setViewName(SIGN_UP_VIEW_NAME);

        if (!signUpDtoUpDto.getPassword().equals(signUpDtoUpDto.getVerifyPassword())) {
            bindingResult.rejectValue("verifyPassword", "NotEquals.verifyPassword");
        }

        if (userService.isNickNameDuplicate(signUpDtoUpDto.getNickName())) {
            bindingResult.rejectValue("nickName", "Duple.nickName");
        }

        if (userService.isPhoneNoDuplicate(signUpDtoUpDto.getPhoneNo())) {
            bindingResult.rejectValue("phoneNo", "Duple.phoneNo");
            return modelAndView;
        }


        if (bindingResult.hasErrors()) {
            return modelAndView;
        }
        SocialSignUpDto signUpDto = socialSignUpValueEncryptoDecryptor.decryptValue(signUpDtoUpDto);
        User user = userService.signUp(signUpDto);

        SocialInfoDto socialInfoDto = new SocialInfoDto(signUpDto.getAuthorizationServerId(), signUpDto.getClientregistrationName(), user);
        socialService.connect(socialInfoDto);
        UserModelDto dto = UserModelDto.toDto(user);

        CustomAuthenticationPrinciple principle = authUser(user, dto);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principle, principle.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);

        modelAndView.setViewName("redirect:/");
        return modelAndView;
    }

    @GetMapping("/kakao/login")
    public String kakaoLogin() {
        return "redirect:/oauth2/authorization/kakao";
    }

    @GetMapping("/naver/login")
    public String naverLogin() {
        return "redirect:/oauth2/authorization/naver";
    }

    @GetMapping("/google/login")
    public String googleLogin() {
        return "redirect:/oauth2/authorization/google";
    }


    @GetMapping("/disconnect/socialType")
    public RedirectView disconnect(@RequestParam("type") String socialType, @AuthenticationPrincipal CustomAuthenticationPrinciple authUser) {
        log.info("소셜 연동 해제");
        socialService.disconnect(authUser.getUsername(), socialType);
        return new RedirectView("/user/edit");
    }

    private CustomAuthenticationPrinciple authUser(User user, UserModelDto dto) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.authority()));
        return CustomAuthenticationPrinciple.from(dto, authorities, AccountStatus.ACTIVE);
    }
}
