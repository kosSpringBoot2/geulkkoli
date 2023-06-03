package com.geulkkoli.domain.favorites;

import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.service.PostService;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.UserRepository;
import com.geulkkoli.web.post.dto.AddDTO;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private FavoritesRepository favoritesRepository;
    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepository;

    private User user, user02;
    private Post post01, post02;

    @AfterEach
    void afterEach() {
        favoritesRepository.deleteAll();
    }

    @BeforeEach
    void init(){
        User save = User.builder()
                .email("test@naver.com")
                .userName("test")
                .nickName("test")
                .phoneNo("00000000000")
                .password("123")
                .gender("male").build();

        user = userRepository.save(save);

        User save2 = User.builder()
                .email("test2@naver.com")
                .userName("test2")
                .nickName("test2")
                .phoneNo("22222222222")
                .password("123")
                .gender("male").build();

        user02 = userRepository.save(save2);
    }

    @BeforeEach
    void beforeEach () {
        AddDTO addDTO01 = AddDTO.builder()
                .title("testTitle01")
                .postBody("test postbody 01")
                .nickName(user.getNickName())
                .build();
        post01 = postService.savePost(addDTO01, user);

        AddDTO addDTO02 = AddDTO.builder()
                .title("testTitle02")
                .postBody("test postbody 02")
                .nickName(user.getNickName())
                .build();
        post02 = postService.savePost(addDTO02, user);
    }

    @Test
    public void 좋아요_저장 () throws Exception {
        //given

        //when
        Long favoriteId = favoriteService.addFavorite(post01, user);

        //then
        List<Favorites> favoritesList = new ArrayList<>(post01.getFavorites());

        assertThat(favoritesList.size()).isEqualTo(1);
    }

    @Test
    public void 좋아요_한개_찾기 () throws Exception {
        //given
        Long favoriteId = favoriteService.addFavorite(post01, user);

        //when
        List<Favorites> favoritesList = new ArrayList<>(post01.getFavorites());
        Favorites findFavorite = favoritesList.get(0);

        //then
        assertThat(favoriteId).isEqualTo(findFavorite.getFavoritesId());
    }

    @Test
    public void 유저_좋아요_전부_찾기 () throws Exception {
        //given
        Long favoriteId01 = favoriteService.addFavorite(post01, user);
        Long favoriteId02 = favoriteService.addFavorite(post02, user);
        //when
        List<Favorites> userFavoritesList = favoriteService.showFavoriteByUser(user);

        //then
        assertThat(userFavoritesList.size()).isEqualTo(2);
        assertThat(userFavoritesList.contains(favoriteService.findById(favoriteId01))).isTrue();
    }

    @Test
    public void 좋아요_지우기 () throws Exception {
        //given
        Long favoriteId01 = favoriteService.addFavorite(post01, user);
        Long favoriteId02 = favoriteService.addFavorite(post01, user02);

        //when
        Favorites deleteFavorite = favoriteService.findById(favoriteId01);
        favoriteService.undoFavorite(deleteFavorite);
        List<Favorites> postFavoritesList = favoriteService.showFavoriteByPost(post01);

        //then
        assertThat(postFavoritesList.get(0)).isEqualTo(favoriteService.findById(favoriteId02));
        assertThat(postFavoritesList.size()).isEqualTo(1);
    }

    @Test
    public void 유저가_게시글에_좋아요_눌렀는지_체크 () throws Exception {
        //given
        Long favoriteId01 = favoriteService.addFavorite(post01, user);
        Favorites favorites1;
        Favorites favorites2;

        //when
        favorites1 = favoriteService.favoriteCheck(post01, user).get();

        try {
            favorites2 = favoriteService.favoriteCheck(post01, user02).get();
        } catch (NoSuchElementException e) {
            favorites2 = null;
        }
        //then
        assertThat(favorites1.getUser()).isEqualTo(user);
        assertThat(favorites2).isNull();
    }
}