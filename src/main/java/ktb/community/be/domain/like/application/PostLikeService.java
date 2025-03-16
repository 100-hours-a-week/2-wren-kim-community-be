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

import java.time.LocalDateTime;

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
            postLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .isDeleted(false)
                    .softDeleteType(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            postLikeRepository.save(postLike);
            return true;
        } else {
            if (postLike.getIsDeleted()) {
                postLike.restore();
                postLikeRepository.save(postLike);
                return true;
            } else {
                postLike.softDeleteByUser();
                postLikeRepository.save(postLike);
                return false;
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
