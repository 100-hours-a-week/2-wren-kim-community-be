package ktb.community.be.domain.post.dao;

import ktb.community.be.domain.post.domain.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    @Query("SELECT i FROM PostImage i WHERE i.post.id = :postId AND i.isDeleted = false")
    List<PostImage> findAllByPostId(@Param("postId") Long postId);
}
