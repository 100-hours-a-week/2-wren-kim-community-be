package ktb.community.be.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ktb.community.be.domain.comment.domain.PostComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"id", "content", "createdAt", "memberNickname", "memberProfileImageUrl", "parentCommentId", "isDeleted", "replies"})
@Getter
@Builder
public class CommentResponseDto {

    private final Long id;
    private final String content;
    private final LocalDateTime createdAt;
    private final String memberNickname;
    private final String memberProfileImageUrl;
    private final Long parentCommentId;
    private final boolean isDeleted;
    @Builder.Default
    private final List<CommentResponseDto> replies = new ArrayList<>();

    public static CommentResponseDto from(PostComment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .memberNickname(comment.getMember().getNickname() != null ? comment.getMember().getNickname() : "(알수없음)")
                .memberProfileImageUrl(comment.getMember().getProfileImageUrl())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .isDeleted(comment.getIsDeleted())
                .build();
    }

    public static CommentResponseDto deleted(PostComment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content("삭제된 댓글입니다.")
                .createdAt(comment.getCreatedAt())
                .memberNickname("(알수없음)")
                .memberProfileImageUrl(null)
                .parentCommentId(null)
                .isDeleted(true)
                .build();
    }
}
