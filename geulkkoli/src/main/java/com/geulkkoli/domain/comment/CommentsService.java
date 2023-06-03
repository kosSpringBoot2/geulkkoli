package com.geulkkoli.domain.comment;

import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.user.User;
import com.geulkkoli.web.comment.dto.CommentBodyDTO;
import com.geulkkoli.web.comment.dto.CommentEditDTO;
import com.geulkkoli.web.comment.dto.CommentListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CommentsService {

    private final CommentsRepository commentsRepository;

    public CommentsService(CommentsRepository commentsRepository) {
        this.commentsRepository = commentsRepository;
    }

    // 댓글 달기
    public void writeComment(CommentBodyDTO commentBody, Post post, User user) {
        commentsRepository.save(user.writeComment(commentBody, post));
    }

    // 댓글 볼러오기
    public static List<CommentListDTO> getCommentsList(Set<Comments> comments) {
        List<CommentListDTO> listDTO = new ArrayList<>();
        for (Comments comment : comments) {
            listDTO.add(CommentListDTO.toDTO(comment));
        }
        return listDTO;
    }

    // 댓글 수정하기
    public void editComment(Long commentId, CommentEditDTO commentEditDTO, User user) {
        commentsRepository.save(user.editComment(commentId, new Comments(commentEditDTO.getCommentBody())));
    }

    // 댓글 지우기
    public void deleteComment(Long commentId, User user) {
        commentsRepository.delete(user.deleteComment(commentId));
    }

    // 지정한 유저의 댓글 불러오기
    public Set<Comments> getUserComment(User user) {
        return commentsRepository.findAllByUser_UserId(user.getUserId());
    }
}