package ktb.community.be.domain.like.domain;

public enum SoftDeleteType {
    USER_ACTION,      // 사용자가 직접 좋아요 취소
    POST_DELETION     // 게시글이 삭제되어 좋아요도 Soft Delete
}
