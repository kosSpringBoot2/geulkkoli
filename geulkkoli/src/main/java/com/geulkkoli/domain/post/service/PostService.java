package com.geulkkoli.domain.post.service;

import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.PostRepository;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.UserRepository;
import com.geulkkoli.web.post.dto.AddDTO;
import com.geulkkoli.web.post.dto.EditDTO;
import com.geulkkoli.web.post.dto.PostRequestListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Post findById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found id matches:" + postId));
    }

    @Transactional
    //게시글 상세보기만을 담당하는 메서드
    public Post showDetailPost(Long postId) {
        postRepository.updateHits(postId);
        return postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found id matches:" + postId));
    }

    @Transactional(readOnly = true)
    public Page<PostRequestListDTO> findAll(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(post -> new PostRequestListDTO(
                post.getPostId(),
                post.getTitle(),
                post.getNickName(),
                post.getUpdatedAt(),
                post.getPostHits()
        ));
    }

    @Transactional(readOnly = true)
    //전체 게시글 리스트 & 조회 기능 포함
    public Page<PostRequestListDTO> searchPostFindAll(Pageable pageable, String searchType, String searchWords) {
        Page<Post> posts;
        switch (searchType) {
            case "제목":
                posts = postRepository.findPostsByTitleContaining(pageable, searchWords);
                break;
            case "본문":
                posts = postRepository.findPostsByPostBodyContaining(pageable, searchWords);
                break;
            case "닉네임":
                posts = postRepository.findPostsByNickNameContaining(pageable, searchWords);
                break;
            default:
                posts = postRepository.findAll(pageable);
                break;
        }
        return posts.map(post -> new PostRequestListDTO(
                post.getPostId(),
                post.getTitle(),
                post.getNickName(),
                post.getUpdatedAt(),
                post.getPostHits()
        ));
    }

    @Transactional
    public Post savePost(AddDTO post, User user) {
        Post writePost = user.writePost(post);
        return postRepository.save(writePost);
    }

    @Transactional
    public void updatePost(Long postId, EditDTO updateParam) {
        Post post = findById(postId)
                .getUser()
                .editPost(postId, updateParam);
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, String nickName) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found id matches:" + postId));
        Optional<User> byNickName = userRepository.findByNickName(nickName);
        if (byNickName.isPresent() && post.getUser().equals(byNickName.get())) {
            User user = byNickName.get();
            user.deletePost(post);
        }

        postRepository.delete(post);
    }

    @Transactional
    public void deleteAll() {
        postRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<LocalDate> getCreatedAts(User user) {
        Set<String> createdAt = postRepository.findCreatedAt(user.getUserId());
        return createdAt.stream()
                .map(postingDate -> LocalDateTime.parse(postingDate, DateTimeFormatter.ofPattern("yyyy. MM. dd a hh:mm:ss")))
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toList());
    }

}
