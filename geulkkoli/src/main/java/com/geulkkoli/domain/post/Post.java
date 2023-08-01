package com.geulkkoli.domain.post;

import com.geulkkoli.domain.admin.Report;
import com.geulkkoli.domain.comment.Comments;
import com.geulkkoli.domain.favorites.Favorites;
import com.geulkkoli.domain.hashtag.HashTag;
import com.geulkkoli.domain.posthashtag.PostHashTag;
import com.geulkkoli.domain.user.NoSuchCommnetException;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.web.post.dto.AddDTO;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Getter
@NoArgsConstructor
@Entity
public class Post extends ConfigDate {

    @Id @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    //게시글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;


    @Column(name = "body", nullable = false, length = 15000)
    private String postBody;

    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Column(nullable = false)
    private int postHits;

    @Column(name = "image_upload_name")
    private String imageUploadName;

    //댓글의 게시글 매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Comments> comments = new LinkedHashSet<>();

    //좋아요의 게시글 매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Favorites> favorites = new LinkedHashSet<>();

    //해시태그의 게시글 매핑
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<PostHashTag> postHashTags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reportedPost", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Report> report = new LinkedHashSet<>();

    @Builder
    public Post(String title, String postBody, String nickName, User user) {
        this.title = title;
        this.postBody = postBody;
        this.nickName = nickName;
        this.user = user;
    }

    //==연관관계 메서드==//

    //제목을 바꾼다.
    public void changeTitle(String title) {
        this.title = title;
    }

    //본문을 바꾼다.
    public void changePostBody(String updateBody) {
        this.postBody = updateBody;
    }

    //별명을 바꾼다.
    public void changeNickName(String nickName) {
        this.nickName = nickName;
    }

    //조회수를 바꾼다.
    public void changeHits (int postHits) {
        this.postHits = postHits;
    }

    /**
     *  게시글로 부터 댓글
     */
    // 게시글에서 댓글 찾기
    private Comments bringComment (Long commentId) {
        return this.comments.stream()
                .filter(comment -> comment.getCommentId().equals(commentId))
                .findFirst()
                .orElseThrow(()->new NoSuchCommnetException("해당 댓글이 없습니다."));
    }

    /**
     *  게시글로 부터 좋아요
     */
    // 게시글에서 누른 좋아요 찾기
    private Favorites bringFavorite (Long favoriteId) {
        return this.favorites.stream()
                .filter(favorite -> favorite.getFavoritesId().equals(favoriteId))
                .findFirst()
                .orElseThrow(()->new NoSuchCommnetException("해당 좋아요가 없습니다."));
    }

    /**
     * 게시글 작성 시 해시태그
     */
    public PostHashTag addHashTag (HashTag hashTag) {
        PostHashTag postHashTag = new PostHashTag(this, hashTag);
        this.getPostHashTags().add(postHashTag);
        hashTag.getPostHashTags().add(postHashTag);
        return postHashTag;
    }

    private PostHashTag findPostHashTag (Long postHashTagId) {
        return this.postHashTags.stream()
                .filter(postHashTag -> postHashTag.getPostHashTagId().equals(postHashTagId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("해당 게시글해시태그가 없습니다."));
    }

    public PostHashTag deletePostHashTag (Long postHashTagId) {
        PostHashTag deletePostHashTag = findPostHashTag(postHashTagId);
        postHashTags.remove(deletePostHashTag);
        deletePostHashTag.getHashTag().getPostHashTags().remove(deletePostHashTag);
        return deletePostHashTag;
    }

    public void deleteAllPostHashTag () {
        this.postHashTags.removeAll(postHashTags);
    }

    @Override
    public String toString() {
        return "Post{" +
                "title='" + title + '\'' +
                ", postBody='" + postBody + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}