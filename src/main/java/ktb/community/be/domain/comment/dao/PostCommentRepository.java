package ktb.community.be.domain.comment.dao;

import ktb.community.be.domain.comment.domain.PostComment;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
//    @Query("SELECT c FROM PostComment c " +
//            "LEFT JOIN FETCH c.user " +
//            "LEFT JOIN FETCH c.parentComment " +
//            "WHERE c.post.id = :postId " +
//            "ORDER BY c.createdAt ASC")
//    List<PostComment> findAllByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM PostComment c WHERE c.post.id = :postId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    @BatchSize(size = 20)
    List<PostComment> findAllByPostId(@Param("postId") Long postId);
}
