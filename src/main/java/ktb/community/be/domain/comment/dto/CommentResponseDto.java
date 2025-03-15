package ktb.community.be.domain.comment.dto;

import ktb.community.be.domain.comment.domain.PostComment;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommentResponseDto {
    private Long id;
    private String content;
    private String userNickname;
    private String userProfileImageUrl;
    private Long parentCommentId;
    private List<CommentResponseDto> replies = new ArrayList<>();

    public CommentResponseDto(PostComment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.userNickname = comment.getUser().getNickname();
        this.userProfileImageUrl = comment.getUser().getProfileImageUrl();
        this.parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
    }

    // 삭제된 댓글 처리 생성자
    public CommentResponseDto(Long id, String content) {
        this.id = id;
        this.content = content;
        this.userNickname = null; // 삭제된 댓글이므로 유저 정보 없음
        this.userProfileImageUrl = null;
        this.parentCommentId = null;
        this.replies = new ArrayList<>();
    }
}
