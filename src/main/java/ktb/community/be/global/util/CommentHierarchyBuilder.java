package ktb.community.be.global.util;

import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentResponseDto;

import java.util.*;

public class CommentHierarchyBuilder {

    private CommentHierarchyBuilder() {}

    public static List<CommentResponseDto> buildCommentHierarchy(List<PostComment> comments) {
        if (comments.isEmpty()) return Collections.emptyList();

        Map<Long, CommentResponseDto> commentMap = new HashMap<>();
        List<CommentResponseDto> topLevelComments = new ArrayList<>();

        // 모든 댓글을 Map에 저장 (삭제된 댓글 포함)
        for (PostComment comment : comments) {
            CommentResponseDto commentDto = CommentResponseDto.from(comment); // 통일
            commentMap.put(comment.getId(), commentDto);
        }

        // 부모-자식 관계를 올바르게 매핑
        for (PostComment comment : comments) {
            Long parentId = Optional.ofNullable(comment.getParentComment())
                    .map(PostComment::getId)
                    .orElse(null);

            CommentResponseDto currentComment = commentMap.get(comment.getId());

            if (parentId == null) {
                topLevelComments.add(currentComment);
            } else {
                // 부모 댓글이 삭제되었더라도 계층 유지
                CommentResponseDto parentComment = commentMap.get(parentId);
                if (parentComment != null) {
                    parentComment.getReplies().add(currentComment);
                }
            }
        }

        // 최상위 댓글을 정렬 (삭제된 댓글이 중간에 끼지 않도록)
        topLevelComments.sort(Comparator.comparing(CommentResponseDto::getCreatedAt));

        return topLevelComments;
    }
}
