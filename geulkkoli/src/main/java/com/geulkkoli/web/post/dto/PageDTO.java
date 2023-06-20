package com.geulkkoli.web.post.dto;

import com.geulkkoli.domain.comment.Comments;
import com.geulkkoli.domain.comment.CommentsService;
import com.geulkkoli.domain.hashtag.HashTag;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.posthashtag.PostHashTag;
import com.geulkkoli.web.comment.dto.CommentListDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@ToString
public class PageDTO {

    @NotBlank
    private Long postId;

    @NotBlank
    private Long authorId;

    @NotEmpty
    @Setter
    private String title;

    @NotEmpty
    @Setter
    private String postBody;

    @NotEmpty
    @Setter
    private String nickName;

    @NotEmpty
    @Setter
    private String date;

    @Setter
    private int favoriteCount;

    @NotBlank
    @Setter
    private List<CommentListDTO> commentList;

    @Setter
    private List<HashTag> tagList = new ArrayList<>();

    @Builder
    public PageDTO(Long postId, Long authorId, String title,
                   String postBody, String nickName, Set<Comments> comments,
                   String date, int favoriteCount, Set<PostHashTag> tagList) {
        this.postId = postId;
        this.authorId = authorId;
        this.title = title;
        this.postBody = postBody;
        this.nickName = nickName;
        this.commentList = CommentsService.getCommentsList(comments);
        this.date = date;
        this.favoriteCount = favoriteCount;
        for(PostHashTag list : tagList) {
            this.tagList.add(list.getHashTag());
        }
    }

    public static PageDTO toDTO (Post post) {
        return PageDTO.builder()
                .postId(post.getPostId())
                .authorId(post.getUser().getUserId())
                .title(post.getTitle())
                .postBody(post.getPostBody())
                .nickName(post.getNickName())
                .comments(post.getComments())
                .date(post.getUpdatedAt())
                .favoriteCount(post.getFavorites().size())
                .tagList(post.getPostHashTags())
                .build();
    }
}
