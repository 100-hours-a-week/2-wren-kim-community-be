package ktb.community.be.domain.post.dao;

import ktb.community.be.domain.post.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author"}) // author는 @EntityGraph로 로딩
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.comments " +  // fetch join은 하나만 사용
            "WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Post> findByIdAndDeletedAtIsNull(@Param("id") Long id);
}
