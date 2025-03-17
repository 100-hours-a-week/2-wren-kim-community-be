package ktb.community.be.domain.like.dao;

import ktb.community.be.domain.like.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
    게시글 상세 조회
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.isDeleted = false")
    int countByPostId(@Param("postId") Long postId);

    /**
    게시글 삭제
     */
    @Query("SELECT pl FROM PostLike pl WHERE pl.post.id = :postId AND pl.isDeleted = false")
    List<PostLike> findAllByPostId(@Param("postId") Long postId);

    /**
     * 특정 사용자와 게시글에 대한 좋아요 조회 (Soft Delete 포함)
     */
    @Query("SELECT pl FROM PostLike pl WHERE pl.post.id = :postId AND pl.member.id = :memberId")
    Optional<PostLike> findByPostIdAndMemberId(@Param("postId") Long postId, @Param("memberId") Long memberId);
}
