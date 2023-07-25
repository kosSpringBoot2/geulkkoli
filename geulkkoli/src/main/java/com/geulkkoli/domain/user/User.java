package com.geulkkoli.domain.user;

import com.geulkkoli.application.security.LockExpiredTimeException;
import com.geulkkoli.application.security.Role;
import com.geulkkoli.application.security.RoleEntity;
import com.geulkkoli.domain.admin.AccountLock;
import com.geulkkoli.domain.admin.Report;
import com.geulkkoli.domain.follow.Follow;
import com.geulkkoli.domain.post.Post;
import com.geulkkoli.domain.comment.Comments;
import com.geulkkoli.domain.favorites.Favorites;
import com.geulkkoli.web.comment.dto.CommentBodyDTO;
import com.geulkkoli.web.comment.dto.CommentEditDTO;
import com.geulkkoli.web.post.dto.AddDTO;
import com.geulkkoli.web.post.dto.EditDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "users", indexes = @Index(name = "idx_user_email_nick_name", columnList = "email,nick_name"))
public class User extends ConfigDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nick_name", nullable = false, unique = true)
    private String nickName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_No", unique = true)
    private String phoneNo;

    @Column(nullable = false)
    private String gender;

    /**
     * report의 필드 User reporter와 대응
     * casacde = CascadeType.AlL:: addReport할때 report.reporter()에 값을 넣으면 같이 영속성에 저장되게 하는 설정
     * report가 단일 소유가 아니기에 설정을 취소한다 추후에 post,like,comment일 때 적용하자
     */
    @OneToMany(mappedBy = "reporter")
    private Set<Report> reports = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @OneToMany(mappedBy = "lockedUser")
    private Set<AccountLock> accountLocks = new LinkedHashSet<>();

    //게시글의 유저 매핑
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts = new LinkedHashSet<>();

    //댓글의 유저 매핑
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comments> comments = new LinkedHashSet<>();

    //좋아요의 유저 매핑
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Favorites> favorites = new LinkedHashSet<>();

    //팔로우의 유저 매핑
    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> followees = new LinkedHashSet<>();

    @Builder
    public User(String userName, String password, String nickName, String email, String phoneNo, String gender) {
        this.userName = userName;
        this.password = password;
        this.nickName = nickName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.gender = gender;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    /**
     * 게시글 관련 CRUD
     */
    //유저가 쓴 게시글
    public Post writePost(AddDTO addDTO) {
        Post post = Post.builder()
                .title(addDTO.getTitle())
                .postBody(addDTO.getPostBody())
                .user(this)
                .nickName(addDTO.getNickName())
                .build();

        this.posts.add(post);
        return post;
    }

    public Post deletePost(Post post) {
        posts.remove(post);
        return post;
    }

    // 해당 유저가 쓴 게시글 찾기

    // 유저가 쓴 게시글 수정하기
    public Post editPost(Long postId, EditDTO editDTO) {
        Post post = this.posts.stream()
                .filter(p -> p.getPostId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new NoSuchPostException("해당 게시글이 없습니다."));

        post.changePostBody(editDTO.getPostBody());
        post.changeTitle(editDTO.getTitle());
        return post;
    }

    public Post editPost(Post post, EditDTO editDTO) {
        post.changePostBody(editDTO.getPostBody());
        post.changeTitle(editDTO.getTitle());
        return post;
    }

    /**
     * 댓글 관련 CRUD
     */
    //유저가 쓴 댓글
    public Comments writeComment(CommentBodyDTO commentBody, Post post) {
        Comments comment = new Comments(this, post, commentBody.getCommentBody());
        post.getComments().add(comment);
        this.comments.add(comment);
        return comment;
    }

    // 해당 유저가 쓴 댓글 찾기
    private Comments findComment(Long commentId) {
        return this.comments.stream()
                .filter(comment -> comment.getCommentId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommnetException("해당 댓글이 없습니다."));
    }

    // 유저가 쓴 댓글 수정하기
    public Comments editComment(CommentEditDTO editCommentBody) {
        Comments comment = findComment(editCommentBody.getCommentId());
        comment.changeComments(editCommentBody.getCommentBody());
        return comment;
    }

    //유저가 지운 댓글
    public Comments deleteComment(Long commentId) {
        Comments deleteComment = findComment(commentId);
        comments.remove(deleteComment);
        deleteComment.getPost().getComments().remove(deleteComment);
        return deleteComment;
    }

    /**
     * 좋아요 관련 CRUD
     */
    // 유저가 누른 좋아요
    public Favorites pressFavorite(Post post) {
        Favorites favorite = new Favorites(this, post);
        post.getFavorites().add(favorite);
        this.favorites.add(favorite);
        return favorite;
    }

    // 해당 유저가 쓴 좋아요 찾기
    private Favorites findFavorite(Long favoriteId) {
        return this.favorites.stream()
                .filter(favorite -> favorite.getFavoritesId().equals(favoriteId))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommnetException("해당 좋아요가 없습니다."));
    }

    // 유저가 취소한 좋아요
    public Favorites cancelFavorite(Long favoriteId) {
        Favorites deleteFavorite = findFavorite(favoriteId);
        favorites.remove(deleteFavorite);
        deleteFavorite.getPost().getFavorites().remove(deleteFavorite);
        return deleteFavorite;
    }

    /**
     * 신고 관련 CRUD
     */
    public Report writeReport(Post post, String reason) {
        Report report = Report.of(post, this, LocalDateTime.now(), reason);
        this.reports.add(report);
        return report;
    }

    public Report deleteReport(Report report) {
        Report report1 = reports.stream().filter(r -> r.equals(report)).findFirst().orElseThrow(() -> new NoSuchReportException("해당 신고가 없습니다."));
        reports.remove(report1);
        return report1;
    }

    /**
     * @param reason             잠김 이유가 들어옵니다.
     * @param lockExpirationDate 만료 날짜가 들어옵니다.
     */
    public AccountLock lock(String reason, LocalDateTime lockExpirationDate) {
        AccountLock accountLock = AccountLock.of(this, reason, lockExpirationDate);
        this.accountLocks.add(accountLock);
        return accountLock;
    }

    //계정이 잠금 여부를 확인하는 메서드 입니다.
    public Boolean isLock() {
        if (CollectionUtils.isEmpty(this.accountLocks)) {
            return false;
        }

        checkLockExpiredDate();

        return this.accountLocks.stream()
                .map(AccountLock::getLockExpirationDate)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new LockExpiredTimeException("계정 잠금 기간이 설정되지 않았습니다."))
                .isAfter(LocalDateTime.now());
    }

    private void checkLockExpiredDate() {
        for (AccountLock accountLock : this.accountLocks) {
            if (Objects.isNull(accountLock.getLockExpirationDate())) {
                throw new LockExpiredTimeException("계정 잠금 기간이 설정되지 않았습니다.");
            }
        }
    }


    public RoleEntity addRole(Role role) {
        RoleEntity roleEntity = RoleEntity.of(role, this);
        this.role = roleEntity;
        return roleEntity;
    }

    public Follow follow(User followee) {
        Follow follow = Follow.of(followee, this);
        this.followees.add(follow);
        return follow;
    }

    public Follow unfollow(Follow follow) {
        this.followees.remove(follow);
        return follow;
    }

    public RoleEntity getRole() {
        return role;
    }

    public String roleName() {
        return role.getRole().getRoleName();
    }

    public Boolean isAdmin() {
        return role.isAdmin();
    }


    public Boolean isUser() {
        return role.isUser();
    }

    public String authority() {
        return role.authority();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}





