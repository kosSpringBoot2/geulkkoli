package com.geulkkoli.web.post.dto;

import com.geulkkoli.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@ToString
public class PageDTO {

    @NotBlank
    private Long postId;

    @NotEmpty
    @Setter
    private String title;

    @NotEmpty
    @Setter
    private String postBody;

    @NotEmpty
    @Setter
    private String nickName;

    @Builder
    public PageDTO(Long postId, String title, String postBody, String nickName) {
        this.postId = postId;
        this.title = title;
        this.postBody = postBody;
        this.nickName = nickName;
    }

    public static PageDTO toDTO (Post post) {
        return PageDTO.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .postBody(post.getPostBody())
                .nickName(post.getNickName())
                .build();
    }
}
