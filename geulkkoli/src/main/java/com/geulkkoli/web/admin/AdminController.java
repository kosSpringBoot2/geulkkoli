package com.geulkkoli.web.admin;

import com.geulkkoli.domain.admin.service.AdminService;
import com.geulkkoli.domain.post.AdminTagAccessDenied;
import com.geulkkoli.domain.post.service.PostService;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.UserFindService;
import com.geulkkoli.web.post.dto.AddDTO;
import com.geulkkoli.web.post.dto.EditDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final UserFindService userFindService;

    private final PostService postService;

    @GetMapping("/") // 어드민 기본 페이지 링크
    public String adminIndex(Model model) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(1);
        List<DailyTopicDto> data = new ArrayList<>();

        data.add(DailyTopicDto.builder().date(DailyTopicDto.dateToString(startDate)).theme("꽃").build());
        data.add(DailyTopicDto.builder().date(DailyTopicDto.dateToString(startDate.plusDays(2))).theme("이").build());
        data.add(DailyTopicDto.builder().date(DailyTopicDto.dateToString(startDate.plusDays(3))).theme("피").build());
        data.add(DailyTopicDto.builder().date(DailyTopicDto.dateToString(startDate.plusDays(4))).theme("는").build());
        data.add(DailyTopicDto.builder().date(DailyTopicDto.dateToString(startDate.plusDays(5))).theme("계").build());
        data.add(DailyTopicDto.builder().date(DailyTopicDto.dateToString(startDate.plusDays(6))).theme("절").build());
        data.add(DailyTopicDto.builder().date(DailyTopicDto.dateToString(startDate.plusDays(7))).theme("엔").build());

        model.addAttribute("data", data);

        return "/admin/adminIndex";
    }

    @ResponseBody
    @PostMapping("/calendar/update")
    public ResponseEntity<Void> updateTheme(@RequestBody DailyTopicDto dailyTopicDto) {
        log.info("date : {}, theme : {}", dailyTopicDto.getDate(), dailyTopicDto.getTheme());
//        LocalDate localDate = LocalDate.parse(dailyThemeDto.getDate(), DateTimeFormatter.BASIC_ISO_DATE);
//        calendarService.updateTheme(localDate, dailyThemeDto.getTheme());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reportedPostList") //신고받은 게시물 링크
    public String reportedPostList(Model model) {
        model.addAttribute("list", adminService.findAllReportedPost());
        return "/admin/reportedPostList";
    }

    //lock user with spring security
    @ResponseBody
    @PostMapping("/lockUser")
    public String lockUser(@RequestBody UserLockDto UserLockDto) {
        log.info("postId : {}, reason : {}, date : {}", UserLockDto.getPostId(), UserLockDto.getLockReason(), UserLockDto.getLockDate());
        User user = adminService.findUserByPostId(UserLockDto.getPostId());
        adminService.lockUser(user.getUserId(), UserLockDto.getLockReason(), UserLockDto.getLockDate());

        String lockDate = (LocalDateTime.now().plusDays(UserLockDto.getLockDate()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return "회원이 \"" + UserLockDto.getLockReason() + "\" 의 사유로" + lockDate + "까지 정지되었습니다.";
    }

    @ResponseBody
    @DeleteMapping("/delete/{postId}")
    public String postDelete(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return "/post/list";
    }

    @GetMapping("/add")
    public String postAddForm(Model model) {
        model.addAttribute("addDTO", new AddDTO());
        return "/admin/noticeAddForm";
    }

    //새 게시글 등록
    @PostMapping("/add")
    public String postAdd(@Validated @ModelAttribute AddDTO post, BindingResult bindingResult,
                          RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        redirectAttributes.addAttribute("page", request.getSession().getAttribute("pageNumber"));

        User user = userFindService.findById(post.getAuthorId());
        long postId = 0;

        if (bindingResult.hasErrors()) {
            return "/admin/noticeAddForm";
        }

        postId = adminService.saveNotice(post, user).getPostId();

        redirectAttributes.addAttribute("postId", postId);
        response.addCookie(new Cookie(URLEncoder.encode(post.getNickName(), StandardCharsets.UTF_8), "done"));
        return "redirect:/post/read/{postId}";
    }

    @GetMapping("/update/{postId}")
    public String movePostEditForm(Model model, @PathVariable Long postId,
                                   @RequestParam(defaultValue = "") String searchType,
                                   @RequestParam(defaultValue = "") String searchWords) {
        EditDTO postPage = EditDTO.toDTO(postService.findById(postId));
        model.addAttribute("editDTO", postPage);
        searchDefault(model, searchType, searchWords);
        return "/admin/noticeEditForm";
    }

    //게시글 수정
    @PostMapping("/update/{postId}")
    public String editPost(@Validated @ModelAttribute EditDTO updateParam, BindingResult bindingResult,
                           @PathVariable Long postId, RedirectAttributes redirectAttributes, HttpServletRequest request,
                           @RequestParam(defaultValue = "") String searchType,
                           @RequestParam(defaultValue = "") String searchWords) {
        try {
            if (bindingResult.hasErrors()) {
                return "/admin/noticeEditForm";
            }
            adminService.updateNotice(postId, updateParam);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("tagCategory", "Tag.Required", new String[]{e.getMessage()},e.toString());
            e.getStackTrace();
        } catch (AdminTagAccessDenied e) {
            bindingResult.rejectValue("tagListString", "Tag.Denied", new String[]{e.getMessage()},e.toString());
            e.getStackTrace();
        }
        if (bindingResult.hasErrors()) {
            return "/admin/noticeEditForm";
        }
        redirectAttributes.addAttribute("updateStatus", true);
        redirectAttributes.addAttribute("page", request.getSession().getAttribute("pageNumber"));
        redirectAttributes.addAttribute("searchType", searchType);
        redirectAttributes.addAttribute("searchWords", searchWords);

        return "redirect:/post/read/{postId}?page={page}&searchType={searchType}&searchWords={searchWords}";
    }

    private static void searchDefault(Model model, String searchType, String searchWords) {
        if (searchType != null && searchWords != null) {
            model.addAttribute("searchType", searchType);
            model.addAttribute("searchWords", searchWords);
        } else {
            model.addAttribute("searchType", "");
            model.addAttribute("searchWords", "");
        }
    }

}
