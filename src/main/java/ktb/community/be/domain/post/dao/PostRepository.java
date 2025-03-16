package ktb.community.be.domain.post.dao;

import ktb.community.be.domain.post.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 게시글 상세 조회
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Post p " +
            "WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Post> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    /**
     * 게시글 상세 조회 시 조회 수 증가
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    /**
     * 커서 기반 게시글 목록 조회 (최신순)
     */
    @Query("SELECT p FROM Post p " +
            "WHERE p.deletedAt IS NULL " +
            "AND (:cursor IS NULL OR p.createdAt < :cursor) " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByCursor(@Param("cursor") LocalDateTime cursor, Pageable pageable);
}
