package ktb.community.be.domain.comment.dto;

import ktb.community.be.domain.comment.domain.PostComment;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommentResponseDto {

    private Long id;
    private String content;
    private String memberNickname;
    private String memberProfileImageUrl;
    private Long parentCommentId;
    private boolean isDeleted;
    private List<CommentResponseDto> replies = new ArrayList<>();

    public CommentResponseDto(PostComment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.memberNickname = comment.getMember().getNickname();
        this.memberProfileImageUrl = comment.getMember().getProfileImageUrl();
        this.parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        this.isDeleted = comment.getIsDeleted();  // 기존 댓글의 삭제 여부 반영
    }

    // 삭제된 댓글 처리 생성자
    public CommentResponseDto(Long id, String content, boolean isDeleted) {
        this.id = id;
        this.content = content;
        this.memberNickname = null; // 삭제된 댓글이므로 유저 정보 없음
        this.memberProfileImageUrl = null;
        this.parentCommentId = null;
        this.isDeleted = isDeleted;
    }
}
