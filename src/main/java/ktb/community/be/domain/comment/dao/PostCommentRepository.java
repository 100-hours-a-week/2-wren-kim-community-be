package ktb.community.be.domain.comment.dao;

import ktb.community.be.domain.comment.domain.PostComment;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
//    @Query("SELECT c FROM PostComment c " +
//            "LEFT JOIN FETCH c.member " +
//            "LEFT JOIN FETCH c.parentComment " +
//            "WHERE c.post.id = :postId " +
//            "ORDER BY c.createdAt ASC")
//    List<PostComment> findAllByPostId(@Param("postId") Long postId);

    /**
     * 댓글 전체 조회
     */
    @Query("SELECT c FROM PostComment c WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    @BatchSize(size = 20)
    List<PostComment> findAllByPostId(@Param("postId") Long postId);

    /**
     * 댓글 수 업데이트
     */
    @Query("SELECT COUNT(c) FROM PostComment c WHERE c.post.id = :postId AND c.deletedAt IS NULL")
    int countByPostId(@Param("postId") Long postId);

    // Soft Delete 포함하여 특정 댓글 조회
    @Query("SELECT c FROM PostComment c LEFT JOIN FETCH c.parentComment WHERE c.id = :commentId")
    Optional<PostComment> findByIdIncludingDeleted(@Param("commentId") Long commentId);
}
