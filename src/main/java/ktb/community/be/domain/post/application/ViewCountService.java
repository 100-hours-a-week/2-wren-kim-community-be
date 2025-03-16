package ktb.community.be.domain.post.application;

import ktb.community.be.domain.post.dao.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final PostRepository postRepository;

    /**
     * 비동기로 조회수 증가
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementViewCount(Long postId) {
        postRepository.incrementViewCount(postId);
    }
}

