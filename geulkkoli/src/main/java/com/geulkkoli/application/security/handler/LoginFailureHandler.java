package com.geulkkoli.application.security.handler;

import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {


    @Autowired
    private MessageSource messageSource;


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("exception = {}", exception.getMessage());
        String errorMessage;
        if (exception instanceof BadCredentialsException) {  // 비밀번호 틀렸을 때
            errorMessage = messageSource.getMessage("error.BadCredentialsException", null, Locale.KOREA);
        } else if (exception instanceof InternalAuthenticationServiceException) { // 인증 과정 중에 일어난 내부 예외
            errorMessage = messageSource.getMessage("error.InternalAuthenticationServiceException", null, Locale.KOREA);
        } else if (exception instanceof UsernameNotFoundException) { // ID가 틀렸을 때
            errorMessage = messageSource.getMessage("error.UsernameNotFoundException", null, Locale.KOREA);
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) { // ID나 비밀번호 입력 안 했을 때
            errorMessage = messageSource.getMessage("error.AuthenticationCredentialsNotFoundException", null, Locale.KOREA);
        } else if (exception instanceof LockedException) {
            errorMessage = messageSource.getMessage("error.LockedException", null, Locale.KOREA);
        } else if (exception instanceof OAuth2AuthenticationException) {
            errorMessage = messageSource.getMessage("error.OAuth2AuthenticationException", null, Locale.KOREA);
        } else {
            errorMessage = messageSource.getMessage("error.OtherException", null, Locale.KOREA);
        }

        log.info("error message {}", errorMessage);

        request.setAttribute("loginError", errorMessage);
        String requestURI = request.getRequestURI();
        log.info("requestURI = {}", requestURI);

        request.setAttribute("loginError", errorMessage);
        request.getRequestDispatcher("/loginPage").forward(request, response);}
}
