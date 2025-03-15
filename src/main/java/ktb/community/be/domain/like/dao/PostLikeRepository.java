package ktb.community.be.domain.like.dao;

import ktb.community.be.domain.like.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /*
    게시글 상세 조회
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.isDeleted = false")
    int countByPostId(@Param("postId") Long postId);

    /*
    게시글 삭제
     */
    void deleteAllByPostId(Long postId);
}
