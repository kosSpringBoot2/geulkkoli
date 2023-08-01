package com.geulkkoli.domain.post.service;

import com.geulkkoli.domain.hashtag.HashTag;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.PostRepository;
import com.geulkkoli.domain.posthashtag.PostHashTag;
import com.geulkkoli.domain.posthashtag.service.PostHashTagService;
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

import java.util.*;

@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostHashTagService postHashTagService;

    public PostService(PostRepository postRepository, UserRepository userRepository, PostHashTagService postHashTagService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postHashTagService = postHashTagService;
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
                String.valueOf(post.getUpdatedAt()),
                post.getPostHits()
        ));
    }

    @Transactional
    public Post savePost(AddDTO addDTO, User user) {
        Post writePost = user.writePost(addDTO);
        Post save = postRepository.save(writePost);
        List<HashTag> hashTags = postHashTagService.hashTagSeparator("#일반" + addDTO.getTagListString() + addDTO.getTagCategory()+ addDTO.getTagStatus());
        log.info("hashTags : " + hashTags);
        postHashTagService.validatePostHasType(hashTags);
        postHashTagService.addHashTagsToPost(save, hashTags);

        return save;
    }

    @Transactional
    public void updatePost(Long postId, EditDTO updateParam) {
        Post post = findById(postId)
                .getUser()
                .editPost(postId, updateParam);
        ArrayList<PostHashTag> postHashTags = new ArrayList<>(post.getPostHashTags());
        if (updateParam.getTagListString() != null && !updateParam.getTagListString().equals("")) {
            for (PostHashTag postHashTag : postHashTags) {
                post.deletePostHashTag(postHashTag.getPostHashTagId());
            }
            List<HashTag> hashTags = postHashTagService.hashTagSeparator("#일반"
                    + updateParam.getTagListString() + updateParam.getTagCategory() + updateParam.getTagStatus());
            postHashTagService.validatePostHasType(hashTags);
            postHashTagService.addHashTagsToPost(post, hashTags);
        }
        postRepository.save(post);
    }



    @Transactional
    public void deletePost(Long postId, Long loggingUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found id matches:" + postId));
        Optional<User> loggingUser = userRepository.findById(loggingUserId);
        ArrayList<PostHashTag> postHashTags = new ArrayList<>(post.getPostHashTags());
        if (loggingUser.isPresent() && post.getUser().equals(loggingUser.get())) {
            for (PostHashTag postHashTag : postHashTags) {
                post.deletePostHashTag(postHashTag.getPostHashTagId());
            }
            Post post1 = loggingUser.get().deletePost(post);
            postRepository.save(post1);
        }

        throw new IllegalArgumentException("해당 게시글을 삭제할 권한이 없습니다.");
    }

    @Transactional
    public void deleteAll() {
        postRepository.deleteAll();
    }


}
