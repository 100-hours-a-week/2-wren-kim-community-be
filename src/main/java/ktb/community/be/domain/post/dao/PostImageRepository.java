package ktb.community.be.domain.post.dao;

import ktb.community.be.domain.post.domain.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    /**
    게시글 상세 조회
     */
    @Query("SELECT i FROM PostImage i WHERE i.post.id = :postId AND i.isDeleted = false ORDER BY i.orderIndex")
    List<PostImage> findAllByPostId(@Param("postId") Long postId);
}
