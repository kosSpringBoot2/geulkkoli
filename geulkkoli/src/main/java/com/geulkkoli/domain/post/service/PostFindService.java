package com.geulkkoli.domain.post.service;

import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.PostRepository;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.domain.user.UserRepository;
import com.geulkkoli.web.post.dto.PostRequestListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostFindService {
    private final PostRepository postRepository;

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
                String.valueOf(post.getUpdatedAt()),
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

    @Transactional(readOnly = true)
    public List<String> findPostWriteDate(User user) {
        Set<String> createdAt = postRepository.findCreatedAt(user.getUserId());
        return createdAt.stream().collect(Collectors.toUnmodifiableList());
    }
}
