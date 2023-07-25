package com.geulkkoli.web.user;

import com.geulkkoli.application.follow.FollowInfos;
import com.geulkkoli.application.user.CustomAuthenticationPrinciple;
import com.geulkkoli.application.user.service.PasswordService;
import com.geulkkoli.domain.favorites.Favorites;
import com.geulkkoli.domain.follow.service.FollowFindService;
import com.geulkkoli.domain.follow.service.FollowService;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.service.PostFindService;
import com.geulkkoli.domain.social.service.SocialInfoFindService;
import com.geulkkoli.domain.social.service.SocialInfoService;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.service.UserFindService;
import com.geulkkoli.domain.user.service.UserService;
import com.geulkkoli.web.comment.dto.CommentBodyDTO;
import com.geulkkoli.web.follow.dto.FollowResult;
import com.geulkkoli.web.follow.dto.FollowsCount;
import com.geulkkoli.web.post.UserProfileDTO;
import com.geulkkoli.web.post.dto.PageDTO;
import com.geulkkoli.web.post.dto.PagingDTO;
import com.geulkkoli.web.post.dto.PostRequestListDTO;
import com.geulkkoli.web.user.dto.edit.PasswordEditFormDto;
import com.geulkkoli.web.user.dto.edit.UserInfoEditFormDto;
import com.geulkkoli.web.user.dto.mypage.ConnectedSocialInfos;
import com.geulkkoli.web.user.dto.mypage.calendar.CalendarDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {

    public static final String EDIT_FORM = "user/edit/userInfoEditForm";
    public static final String EDIT_PASSWORD_FORM = "user/edit/passwordEditForm";
    public static final String REDIRECT_INDEX = "redirect:/";
    public static final String REDIRECT_EDIT_INDEX = "redirect:/user/edit";
    private final UserService userService;
    private final UserFindService userFindService;
    private final PostFindService postFindService;
    private final PasswordService passwordService;
    private final FollowFindService followFindService;
    private final SocialInfoService socialInfoService;
    private final FollowService followService;
    private final SocialInfoFindService socialInfoFindService;


    @GetMapping("/{nickName}")
    public ModelAndView getMyPage(@PathVariable("nickName") String nickName) {
        User user = userFindService.findByNickName(nickName);
        Integer followee = followFindService.countFolloweeByFollowerId(user.getUserId());
        Integer follower = followFindService.countFollowerByFolloweeId(user.getUserId());
        FollowsCount followsCount = FollowsCount.of(followee, follower);
        ModelAndView modelAndView = new ModelAndView("user/mypage","nickName", nickName);
        modelAndView.addObject("followsCount", followsCount);
        return modelAndView;
    }

    @GetMapping("{nickName}/calendar")
    @ResponseBody
    public ResponseEntity<CalendarDto> calendaring(@PathVariable("nickName") String nickName) {
        User user = userFindService.findByNickName(nickName);
        List<String> allPostWriteDatesByOneUser = postFindService.findPostWriteDate(user);
        CalendarDto calendarDto = new CalendarDto(user.getUserName(), user.getSignUpDate(), allPostWriteDatesByOneUser);
        log.info("calendarDto : {}", calendarDto.getAllPostDatesByOneUser());
        return ResponseEntity.ok(calendarDto);
    }

    @GetMapping("/{nickName}/followees")
    public ModelAndView getFollowees(@PathVariable("nickName") String nickName, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userFindService.findByNickName(nickName);
        Integer followee = followFindService.countFolloweeByFollowerId(user.getUserId());
        FollowInfos followeeUserInfos = followFindService.findSomeFolloweeByFollowerId(user.getUserId(), null, pageable);
        ModelAndView modelAndView = new ModelAndView("user/mypage/followdetail", "followers", followeeUserInfos);
        modelAndView.addObject("allCount", followee);

        return modelAndView;
    }

    @GetMapping("/{nickName}/followers")
    public ModelAndView getFollowers(@PathVariable("nickName") String nickName, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userFindService.findByNickName(nickName);
        Integer follower = followFindService.countFollowerByFolloweeId(user.getUserId());
        FollowInfos followInfos = followFindService.findSomeFollowerByFolloweeId(user.getUserId(), null, pageable);
        List<Long> userIdByFollowedEachOther = followFindService.findUserIdByFollowedEachOther(followInfos.userIds(), user.getUserId(), pageable.getPageSize());
        followInfos.checkSubscribe(userIdByFollowedEachOther);
        ModelAndView modelAndView = new ModelAndView("user/mypage/followerdetail", "followers", followInfos);
        modelAndView.addObject("allCount", follower);
        return modelAndView;
    }

    @GetMapping("{nickName}/write-posts")
    public ModelAndView getPostList(@PathVariable("nickName") String nickName, @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userFindService.findByNickName(nickName);
        List<Post> posts = user.getPosts().stream().collect(toUnmodifiableList());
        List<Post> subPost = posts.subList(pageable.getPageNumber() * pageable.getPageSize(), Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), posts.size()));
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());
        int totalPosts = posts.size();
        Page<Post> pagePost = new PageImpl<>(subPost, pageRequest, totalPosts);
        Page<PostRequestListDTO> readInfos = pagePost.map(PostRequestListDTO::toDTO);
        PagingDTO pagingDTO = PagingDTO.listDTOtoPagingDTO(readInfos);


        ModelAndView modelAndView = new ModelAndView("user/writepost", "pagingResponses", pagingDTO);
        modelAndView.addObject("loggingNickName", nickName);

        return modelAndView;
    }


    @GetMapping("{nickName}/write-posts/{postId}")
    public ModelAndView readPost(@PathVariable("nickName") String nickName, @PathVariable("postId") Long postId) {
        Post byId = postFindService.findById(postId);

        PageDTO post = PageDTO.toDTO(byId);
        FollowResult followResult = new FollowResult(true, false);
        String checkFavorite = "none";
        UserProfileDTO authorUser = UserProfileDTO.toDTO(byId.getUser());

        ModelAndView modelAndView = new ModelAndView("/post/postPage");
        modelAndView.addObject("post", post);
        modelAndView.addObject("authorUser", authorUser);
        modelAndView.addObject("followResult", followResult);
        modelAndView.addObject("checkFavorite", checkFavorite);
        modelAndView.addObject("commentList", post.getCommentList());
        modelAndView.addObject("comments", new CommentBodyDTO());

        return modelAndView;
    }


    @GetMapping("/{nickName}/favorites")
    public ModelAndView getFavorite(@PathVariable("nickName") String nickName, @PageableDefault(size = 5, sort = "postId", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("nickName = {}", nickName);
        User user = userFindService.findByNickName(nickName);
        List<Favorites> favorites = user.getFavorites().stream().collect(toUnmodifiableList());
        List<Post> favoritePosts = favorites.stream().sorted(Comparator.comparing(Favorites::getFavoritesId).reversed()).map(Favorites::getPost).collect(toList());

        int totalFavoritePosts = favoritePosts.size();
        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), totalFavoritePosts);
        List<Post> subPosts = favoritePosts.subList(startIndex, endIndex);

        Page<Post> favoritsPost = new PageImpl<>(subPosts, pageable, favoritePosts.size());
        Page<PostRequestListDTO> readInfos = favoritsPost.map(PostRequestListDTO::toDTO);
        PagingDTO pagingDTO = PagingDTO.listDTOtoPagingDTO(readInfos);
        log.info("pagingDTO = {}", pagingDTO);
        ModelAndView modelAndView = new ModelAndView("user/favorites", "pagingResponses", pagingDTO);
        modelAndView.addObject("loggingNickName", nickName);

        return modelAndView;
    }

    @GetMapping("{nickName}/favorites/{postId}")
    public ModelAndView getFavoritePost(@PathVariable("nickName") String nickName, @PathVariable("postId") Long postId) {
        User user = userFindService.findByNickName(nickName);
        Post findPost = postFindService.findById(postId);
        PageDTO post = PageDTO.toDTO(findPost);
        FollowResult followResult = new FollowResult(false, followFindService.checkFollow(user, findPost.getUser()));
        String checkFavorite = "exist";

        UserProfileDTO authorUser = UserProfileDTO.toDTO(findPost.getUser());
        ModelAndView modelAndView = new ModelAndView("/post/postPage");
        modelAndView.addObject("post", post);
        modelAndView.addObject("authorUser", authorUser);
        modelAndView.addObject("followResult", followResult);
        modelAndView.addObject("checkFavorite", checkFavorite);
        modelAndView.addObject("commentList", post.getCommentList());
        modelAndView.addObject("comments", new CommentBodyDTO());
        return modelAndView;
    }


    @GetMapping("/edit")
    public ModelAndView editUserInfo(@AuthenticationPrincipal CustomAuthenticationPrinciple authUser) {
        ConnectedSocialInfos connectedInfos = socialInfoFindService.findConnectedInfos(authUser.getUsername());
        UserInfoEditFormDto userInfoEditDto = UserInfoEditFormDto.form(authUser.getUserRealName(), authUser.getNickName(), authUser.getPhoneNo(), authUser.getGender());
        ModelAndView modelAndView = new ModelAndView(EDIT_FORM, "editForm", userInfoEditDto);
        modelAndView.addObject("connectedInfos", connectedInfos);

        modelAndView.setViewName(EDIT_FORM);

        return modelAndView;
    }


    @PostMapping("/edit")
    public String editUserInfo(@Validated @ModelAttribute("editForm") UserInfoEditFormDto userInfoEditDto, BindingResult bindingResult, @AuthenticationPrincipal CustomAuthenticationPrinciple authUser) {
        // 닉네임 중복 검사 && 본인의 기존 닉네임과 일치해도 중복이라고 안 뜨게
        if (userService.isNickNameDuplicate(userInfoEditDto.getNickName()) && !userInfoEditDto.getNickName().equals(authUser.getNickName())) {
            bindingResult.rejectValue("nickName", "Duple.nickName");
        }

        if (userService.isPhoneNoDuplicate(userInfoEditDto.getPhoneNo()) && !userInfoEditDto.getPhoneNo().equals(authUser.getPhoneNo())) {
            bindingResult.rejectValue("phoneNo", "Duple.phoneNo");
        }

        if (bindingResult.hasErrors()) {
            return EDIT_FORM;
        } else {
            userService.edit(parseLong(authUser), userInfoEditDto);
            // 세션에 저장된 authUser의 정보를 수정한다.
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            CustomAuthenticationPrinciple newAuth = (CustomAuthenticationPrinciple) principal;
            newAuth.modifyNickName(userInfoEditDto.getNickName());
            newAuth.modifyPhoneNo(userInfoEditDto.getPhoneNo());
            newAuth.modifyGender(userInfoEditDto.getGender());
            newAuth.modifyUserRealName(userInfoEditDto.getUserName());
        }
        return REDIRECT_INDEX;
    }

    @GetMapping("/edit/edit-password")
    public String editPasswordForm(@ModelAttribute("passwordEditForm") PasswordEditFormDto form) {
        return EDIT_PASSWORD_FORM;
    }

    @PostMapping("/edit/edit-password")
    public ModelAndView editPassword(@Validated @ModelAttribute("passwordEditForm") PasswordEditFormDto form, BindingResult bindingResult, @AuthenticationPrincipal CustomAuthenticationPrinciple authUser) {
        User user = userFindService.findById(parseLong(authUser));
        if (!passwordService.isPasswordVerification(user, form)) {
            bindingResult.rejectValue("oldPassword", "Check.password");
        }

        if (!form.getNewPassword().equals(form.getVerifyPassword())) {
            bindingResult.rejectValue("verifyPassword", "Check.verifyPassword");
        }
        if (bindingResult.hasErrors()) {
            return new ModelAndView(EDIT_PASSWORD_FORM);
        }
        passwordService.updatePassword(parseLong(authUser), form.getNewPassword());
        ModelAndView modelAndView = new ModelAndView(REDIRECT_EDIT_INDEX);
        modelAndView.addObject("status", true);
        return modelAndView;
    }

    /**
     * 서비스에서 쓰는 객체의 이름은 User인데 memberDelete라는 이름으로 되어 있어서 통일성을 위해 이름을 고친다.
     * 또한 사용자 입장에서는 자신의 정보를 삭제하는 게 아니라 탈퇴하는 서비스를 쓰고 있으므로 uri를 의미에 더 가깝게 고쳤다.
     */
    @GetMapping("/{nickName}/unsubscribed")
    public String unSubscribe(@PathVariable("nickName") String nickName) {
        try {
            User findUser = userFindService.findByNickName(nickName);
            followService.allUnfollow(findUser);
            socialInfoFindService.findAllByUser_eamil(findUser.getEmail()).forEach(socialInfoService::delete);
            userService.delete(findUser);
        } catch (Exception e) {
            log.error("unSubscribe error = {}", e.getMessage());
            return REDIRECT_INDEX;
        }
        return REDIRECT_INDEX;
    }

    private Long parseLong(CustomAuthenticationPrinciple authUser) {
        return Long.valueOf(authUser.getUserId());
    }
}
