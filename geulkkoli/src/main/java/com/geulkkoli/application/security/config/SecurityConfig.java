package com.geulkkoli.application.security.config;

import com.geulkkoli.application.security.UserSecurityService;
import com.geulkkoli.application.security.handler.LoginFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 시큐리티 설정파일
 */
@Configuration
public class SecurityConfig {

    private final LoginFailureHandler loginFailureHandler;
    private final UserSecurityService userSecurityService;

    public SecurityConfig(LoginFailureHandler loginFailureHandler, UserSecurityService userSecurityService) {
        this.loginFailureHandler = loginFailureHandler;
        this.userSecurityService = userSecurityService;
    }

    /**
     * 시큐리티 필터 설정
     * 루트 페이지, 로그인 페이지, css,js 경론는 인증 없이 접속 가능
     * csrf 공격 방지를 위한 설정을 끈다
     * 인증방식이 form방식인 걸 알려준다
     * 로그인 폼 페이즈가 어디인지 알려준다
     * 로그인 정보 URI가 어디인지 알려준다
     * 실패시 URL 정보
     * userName 키 이름을 email로 바꿔준다.
     * 비빌먼호 키 이름을 password로 바꿔준다
     * 햐
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.userDetailsService(userSecurityService)
                .authorizeRequests((auth) -> {
                    auth.antMatchers( "/", "/loginPage", "/css/**", "/js/**")
                            .permitAll();
                }).csrf().disable()
                .formLogin()
                .loginPage("/loginPage")
                .loginProcessingUrl("/login-process")
                .defaultSuccessUrl("/index")
                .usernameParameter("email")
                .passwordParameter("password")
                .failureHandler(loginFailureHandler)
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true);

        return http.build();
    }

    /*
     * password를 복호화 해줄 때 사용한다.
     * */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



}

