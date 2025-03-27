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

        // 모든 댓글을 DTO로 변환
        for (PostComment comment : comments) {
            CommentResponseDto commentDto = CommentResponseDto.from(comment);
            commentMap.put(comment.getId(), commentDto);
        }

        for (PostComment comment : comments) {
            Long parentId = Optional.ofNullable(comment.getParentComment())
                    .map(PostComment::getId)
                    .orElse(null);

            CommentResponseDto currentComment = commentMap.get(comment.getId());

            if (parentId == null) {
                // 원댓글
                topLevelComments.add(currentComment);
            } else {
                // 대댓글의 parent가 대댓글이면 최상위 댓글까지 올라감
                PostComment topParent = getTopLevelParent(comment);
                CommentResponseDto topParentDto = commentMap.get(topParent.getId());

                if (topParentDto != null) {
                    topParentDto.getReplies().add(currentComment);
                }
            }
        }

        // 정렬 (원댓글만 정렬)
        topLevelComments.sort(Comparator.comparing(CommentResponseDto::getCreatedAt));
        topLevelComments.forEach(c -> c.getReplies().sort(Comparator.comparing(CommentResponseDto::getCreatedAt)));

        return topLevelComments;
    }

    private static PostComment getTopLevelParent(PostComment comment) {
        PostComment current = comment;
        while (current.getParentComment() != null && current.getParentComment().getParentComment() != null) {
            current = current.getParentComment();
        }
        return current.getParentComment() == null ? current : current.getParentComment();
    }
}
