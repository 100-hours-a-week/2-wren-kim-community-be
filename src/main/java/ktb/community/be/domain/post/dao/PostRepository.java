package ktb.community.be.domain.post.dao;

import ktb.community.be.domain.post.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /*
    게시글 상세 조회
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Post p " +
            "WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Post> findByIdAndDeletedAtIsNull(@Param("id") Long id);
}
