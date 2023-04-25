package com.geulkkoli.domain.post.entity;

import com.geulkkoli.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class HashTags {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashTagsId;

    //어떤 게시글에 해쉬태그가 들어갔는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private String hashTagName;

    @Builder
    public HashTags (String hashTagName) {
        this.hashTagName = hashTagName;
    }

    //==연관관계 메서드==//

    /**
     * 게시글 세팅
     */
    public void addPost (Post post) {
        this.post = post;
        post.getHashTags().add(this);
    }
}