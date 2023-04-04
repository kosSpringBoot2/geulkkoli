package com.geulkkoli.domain.service;

import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.LoginService;
import com.geulkkoli.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class LoginServiceTest {

    @Autowired
    LoginService loginService;
    @Autowired
    UserRepository userRepository;

    @PostConstruct
    void init() {
        userRepository.save(User.builder()
                .userId("kkk")
                .userName("김")
                .nickName("바나나")
                .password("1234")
                .build());
    }

    @Test
    @DisplayName("로그인테스트")
    void loginTest() {
        //given
        User exitsUser = User.builder()
                .userId("kkk")
                .password("1234")
                .build();
        //when
        Optional<User> loginUser = loginService.login("kkk", "1234");

        //then
        assertThat(loginUser)
                .isNotEmpty()
                .hasValue(exitsUser);
    }

    @Test
    @DisplayName("로그인실패시_널을 반환한다.")
    void throwErrorWhenLoginFailedTest() {
        //given
        Optional<User> noneExistentUser = loginService.login("kkk", "1243");
        //when

        //then
        assertThat(noneExistentUser).isEmpty();
    }

}