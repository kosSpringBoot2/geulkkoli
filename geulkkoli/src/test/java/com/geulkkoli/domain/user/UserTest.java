package com.geulkkoli.domain.user;

import com.geulkkoli.application.security.LockExpiredTimeException;
import com.geulkkoli.application.security.Role;
import com.geulkkoli.application.security.RoleEntity;
import com.geulkkoli.domain.admin.Report;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.web.post.dto.AddDTO;
import com.geulkkoli.web.post.dto.EditDTO;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class UserTest {

    @Test
    @DisplayName("계정 잠금 테스트")
    void isLock() {

        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();

        user.lock("reason", LocalDateTime.now().plusDays(7));

        assertTrue(user.isLock());
    }

    @Test
    @DisplayName("계정 잠금 시간이 설정이 되어있지 않다면 LockExpiredTimeException을 던진다")
    void if_no_lock_expiration_time_is_throw_LockedExpiredTimeException() {

        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();
        user.lock("reason", null);

        assertThrows(LockExpiredTimeException.class, user::isLock);
    }

    @Test
    void writeReport() {
        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();
        Post post = Post.builder()
                .nickName("nickName")
                .title("title")
                .postBody("content")
                .build();

        Report report = user.writeReport(post, "reason");

        assertThat(user).has(new Condition<>(u -> u.getReports().contains(report), "report가 추가되었다"));
    }

    @DisplayName("report를 삭제한다")
    @Test
    void deleteReport() {
        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();
        Post post = Post.builder()
                .nickName("nickName")
                .title("title")
                .postBody("content")
                .build();

        Post post1 = Post.builder()
                .nickName("nickName1")
                .title("title1")
                .postBody("content1")
                .build();

        Report report = user.writeReport(post, "reason");
        Report report1 = user.writeReport(post1, "reason1");

        user.deleteReport(report1);

        assertThat(user).has(new Condition<>(u -> !u.getReports().contains(report1), "report가 삭제되었다"));
    }

    @DisplayName("역할을 추가한다")
    @Test
    void addRole() {
        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();
        RoleEntity roleEntity = user.addRole(Role.USER);

        assertThat(roleEntity).isNotNull().has(new Condition<>(r -> r.getRole() == Role.USER, "USER 역할이 추가되었다")).has(new Condition<>(r -> r.getUsers().contains(user), "user가 추가되었다"));
    }

    @DisplayName("글을 작성한다")
    @Test
    void writePost() {
        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();


        AddDTO addDTO = AddDTO.builder()
                .nickName(user.getNickName())
                .authorId(1L)
                .title("title")
                .postBody("content")
                .build();
        Post post = user.writePost(addDTO);

        assertAll(
                () -> assertThat(user).has(new Condition<>(u -> u.getPosts().contains(post), "post가 추가되었다")),
                () -> assertThat(post).has(new Condition<>(p -> p.getUser().equals(user), "user가 추가되었다"))
        );
    }

    @DisplayName("글을 수정한다")
    @Test
    void editPost() {
        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();


        AddDTO addDTO = AddDTO.builder()
                .nickName(user.getNickName())
                .authorId(1L)
                .title("title")
                .postBody("content")
                .build();
        Post post = user.writePost(addDTO);

        EditDTO editDTO = EditDTO.builder()
                .postId(1L)
                .title("title1")
                .postBody("content1")
                .build();

        Post post1 = user.editPost(post, editDTO);

        assertAll(
                () -> assertThat(post1).has(new Condition<>(p -> p.getTitle().equals("title1"), "title이 수정되었다")),
                () -> assertThat(post1).has(new Condition<>(p -> p.getPostBody().equals("content1"), "content가 수정되었다"))
        );
    }

    @DisplayName("글을 삭제한다")
    @Test
    void deletePost() {
        User user = User.builder()
                .email("email@gmail.com")
                .userName("userName")
                .password("password")
                .nickName("nickName")
                .phoneNo("0102221111")
                .gender("male")
                .build();


        AddDTO addDTO = AddDTO.builder()
                .nickName(user.getNickName())
                .authorId(1L)
                .title("title")
                .postBody("content")
                .build();
        Post post = user.writePost(addDTO);
        Post post2 = user.writePost(addDTO);
        Post post1 = user.deletePost(post);

        assertThat(user).has(new Condition<>(u -> !u.getPosts().contains(post1), "post가 삭제되었다"));
    }
}