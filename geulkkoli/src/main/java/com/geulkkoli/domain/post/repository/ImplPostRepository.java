package com.geulkkoli.domain.post.repository;

import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional
@RequiredArgsConstructor
public class ImplPostRepository implements PostRepository {

    private final EntityManager entityManager;

    @Override
    public Post save(Post post) {
        entityManager.persist(post);
        return post;
    }

    @Override
    public Optional<Post> findById(Long postId) {
        Post post = entityManager.find(Post.class, postId);
        return Optional.of(post);
    }

    @Override
    public List<Post> findAll() {
        return entityManager
                .createQuery("select p from Post p", Post.class)
                .getResultList();
    }

    @Override
    public void update (Long postId, Post updateParam) {
        Post findPost = entityManager.find(Post.class, postId);
        findPost.changeTitle(updateParam.getTitle());
        findPost.changePostBody(updateParam.getPostBody());
    }

    @Override
    public void delete (Long postId) {
        Post deletePost = entityManager.find(Post.class, postId);
        entityManager.remove(deletePost);
    }

    public void clear () {
        entityManager.close();
    }
}