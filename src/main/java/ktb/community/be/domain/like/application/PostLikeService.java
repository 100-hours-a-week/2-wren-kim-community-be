package ktb.community.be.domain.like.application;

import ktb.community.be.domain.like.dao.PostLikeRepository;
import ktb.community.be.domain.like.domain.PostLike;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.user.dao.UserRepository;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    /**
     * 사용자가 게시글에 좋아요를 추가/취소하는 기능
     */
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 사용자의 기존 좋아요 확인
        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId).orElse(null);

        if (postLike == null) {
            // 1️⃣ 기존 좋아요 데이터가 없으면 새로 추가
            postLike = new PostLike(post, user);
            postLikeRepository.save(postLike);
            return true; // 좋아요 추가됨
        } else {
            if (postLike.getIsDeleted()) {
                // 2️⃣ Soft Delete 된 경우 → 복구
                postLike.restore();
                postLikeRepository.save(postLike);
                return true; // 좋아요 추가됨
            } else {
                // 3️⃣ 기존 좋아요가 활성화 상태 → Soft Delete 처리
                postLike.softDeleteByUser();
                postLikeRepository.save(postLike);
                return false; // 좋아요 취소됨
            }
        }
    }

    /**
     * 특정 게시글의 좋아요 개수 반환
     */
    @Transactional(readOnly = true)
    public int getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}
