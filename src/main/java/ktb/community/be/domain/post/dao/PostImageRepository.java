package ktb.community.be.domain.post.dao;

import ktb.community.be.domain.post.domain.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
