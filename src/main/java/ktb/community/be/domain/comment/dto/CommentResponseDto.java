package ktb.community.be.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"id", "content", "createdAt", "updatedAt", "memberNickname", "memberProfileImageUrl", "parentCommentId", "isDeleted", "replies"})
@Getter
@Builder
public class CommentResponseDto {

    private final Long id;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String memberNickname;
    private final String memberProfileImageUrl;
    private final Long parentCommentId;
    private final boolean isDeleted;
    @Builder.Default
    private final List<CommentResponseDto> replies = new ArrayList<>();

    public static CommentResponseDto from(PostComment comment) {
        boolean isCommentDeleted = comment.getIsDeleted();
        Member member = comment.getMember();

        boolean isMemberDeleted = member == null || member.getIsDeleted();

        String nickname = isCommentDeleted || isMemberDeleted ? "(알수없음)" : member.getNickname();
        String profileImageUrl = isCommentDeleted || isMemberDeleted ? null : member.getProfileImageUrl();

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(isCommentDeleted ? "삭제된 댓글입니다." : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .memberNickname(nickname)
                .memberProfileImageUrl(profileImageUrl)
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .isDeleted(isCommentDeleted)
                .build();
    }
}
