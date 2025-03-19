package ktb.community.be.global.util;

import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentResponseDto;

import java.util.*;

public class CommentHierarchyBuilder {

    private CommentHierarchyBuilder() {
        // 인스턴스화 방지 (유틸리티 클래스)
    }

    public static List<CommentResponseDto> buildCommentHierarchy(List<PostComment> comments) {
        if (comments.isEmpty()) return Collections.emptyList();

        Map<Long, CommentResponseDto> commentMap = new HashMap<>();
        List<CommentResponseDto> topLevelComments = new ArrayList<>();

        // 모든 댓글을 Map에 저장 (삭제 여부 포함)
        for (PostComment comment : comments) {
            commentMap.put(comment.getId(), CommentResponseDto.from(comment));
        }

        // 부모-자식 관계 설정
        for (PostComment comment : comments) {
            Long parentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;

            if (parentId == null) {
                // 부모 댓글이 없는 경우 최상위 댓글로 처리
                topLevelComments.add(commentMap.get(comment.getId()));
            } else {
                // 부모 댓글 찾기
                CommentResponseDto parentComment = commentMap.get(parentId);

                if (parentComment == null) {
                    // 기존에는 새로운 삭제된 댓글 DTO를 생성했지만, 이제는 isDeleted 필드를 활용하여 기존 방식 유지
                    parentComment = CommentResponseDto.deleted(comment);
                    commentMap.put(parentId, parentComment);

                    // 부모 댓글이 없으면 최상위 댓글 리스트에 추가
                    topLevelComments.add(parentComment);
                }

                // 부모 댓글에 대댓글 추가
                parentComment.getReplies().add(commentMap.get(comment.getId()));
            }
        }

        return topLevelComments;
    }
}
