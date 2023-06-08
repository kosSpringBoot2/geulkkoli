package com.geulkkoli;

import com.geulkkoli.application.user.UserSecurityService;
import com.geulkkoli.domain.admin.Report;
import com.geulkkoli.domain.admin.ReportRepository;
import com.geulkkoli.domain.admin.service.AdminServiceImpl;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.PostRepository;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.UserService;
import com.geulkkoli.web.post.dto.AddDTO;
import com.geulkkoli.web.user.dto.JoinFormDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("dev")
public class TestDataInit {

    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final AdminServiceImpl adminServiceImpl;
    private final UserService userService;

    /**
     * 확인용 초기 데이터 추가
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("test data init");

        /*
         * 신고받은 게시물 더미 데이터*/

        /*
         * 시큐리티가 제공하는 비밀번호 암호화를 userService에서 쓰기 때문에
         * userService로 테스트 데이터를 저정한다.
         * */

        JoinFormDto joinForm = new JoinFormDto();
        joinForm.setEmail("tako99@naver.com");
        joinForm.setUserName("김");
        joinForm.setNickName("바나나11");
        joinForm.setPhoneNo("9190232333");
        joinForm.setGender("male");
        joinForm.setPassword("qwe123!!!");
        User user = userService.signUp(joinForm);


//        JoinFormDto joinForm1 = new JoinFormDto();
//        joinForm1.setEmail("kimpjh1@naver.com");
//        joinForm1.setUserName("김");
//        joinForm1.setNickName("바나나121");
//        joinForm1.setPhoneNo("9290232333");
//        joinForm1.setGender("male");
//        joinForm1.setPassword("123");
//        userService.signUp(joinForm1);

        JoinFormDto joinForm2 = new JoinFormDto();
        joinForm2.setEmail("test01@naver.com");
        joinForm2.setUserName("테스트유저");
        joinForm2.setNickName("준호떡볶이도둑");
        joinForm2.setPhoneNo("01012345678");
        joinForm2.setGender("male");
        joinForm2.setPassword("123");
        userService.signUp(joinForm2);

        JoinFormDto joinForm3 = new JoinFormDto();
        joinForm3.setEmail("cheese@naver.com");
        joinForm3.setUserName("비밀");
        joinForm3.setNickName("김륜환만세");
        joinForm3.setPhoneNo("01099995555");
        joinForm3.setGender("female");
        joinForm3.setPassword("123");
        userSecurityService.join(joinForm3);

        joinForm.setEmail("admin");
        joinForm.setUserName("타코다치");
        joinForm.setNickName("우무문어");
        joinForm.setPhoneNo("01033132232");
        joinForm.setGender("male");
        joinForm.setPassword("123");
        userService.signUpAdmin(joinForm);

        JoinFormDto joinForm4 = new JoinFormDto();
        joinForm4.setEmail("calendar@naver.com");
        joinForm4.setUserName("김캘린");
        joinForm4.setNickName("캘린더");
        joinForm4.setPhoneNo("01098765432");
        joinForm4.setGender("female");
        joinForm4.setPassword("123");
        LocalDate signUpDate = LocalDate.of(2022, 1, 1);
        User user4 = userSecurityService.join(joinForm4, signUpDate);

        User user01 = userService.findById(1L);

        for (int i = 0; i < 4; ++i) {

            AddDTO addDTO = AddDTO.builder()
                    .title("여러분")
                    .postBody("나는 멋지고 섹시한 개발자")
                    .nickName(user01.getNickName())
                    .build();
            Post post = user01.writePost(addDTO);
            postRepository.save(post);

            AddDTO addDTO1 = AddDTO.builder()
                    .title("testTitle01")
                    .postBody("test postbody 01")
                    .nickName(user01.getNickName())
                    .build();
            Post post1 = user01.writePost(addDTO1);
            postRepository.save(post1);

            AddDTO addDTO2 = AddDTO.builder()
                    .title("testTitle02")
                    .postBody("test postbody 02")
                    .nickName(user01.getNickName())
                    .build();
            Post post2 = user01.writePost(addDTO2);
            postRepository.save(post2);

            AddDTO addDTO3 = AddDTO.builder()
                    .title("testTitle03")
                    .postBody("test postbody 03")
                    .nickName(user01.getNickName())
                    .build();
            Post post3 = user01.writePost(addDTO3);
            postRepository.save(post3);
        }

        for (int i = 7; i <= 12; i++) {
            AddDTO addDTO = AddDTO.builder()
                    .title("달력 테스트")
                    .postBody("햄버거")
                    .nickName(user4.getNickName())
                    .build();
            LocalDateTime createAt = LocalDateTime.of(2022, i, i, 1, 1);
            Post post4 = user4.writePost(addDTO, createAt);
            postRepository.save(post4);
        }

        for (int i = 1; i <= 6; i++) {
            AddDTO addDTO = AddDTO.builder()
                    .title("달력 테스트")
                    .postBody("햄버거")
                    .nickName(user4.getNickName())
                    .build();
            LocalDateTime createAt = LocalDateTime.of(2023, i, i, 1, 1);
            Post post4 = user4.writePost(addDTO, createAt);
            postRepository.save(post4);
        }


        /**
         * 신고받은 게시물 더미 데이터를 리팩토링한 방식으로 다시 작성해봤습니다.
         */
        Report report = user.writeReport(postRepository.findById(2L).get(), "욕설");
        Report report1 = user.writeReport(postRepository.findById(1L).get(), "비 협조적");
        Report report2 = user.writeReport(postRepository.findById(4L).get(), "점심을 안먹음");
        reportRepository.save(report);
        reportRepository.save(report1);
        reportRepository.save(report2);


//        Follow follow1 = user.writeFollow(followRepository.findBySelectUserId(1L).get().getFolloweeId());
//        followRepository.save(follow1);
    }


}
