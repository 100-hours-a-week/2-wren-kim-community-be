package ktb.community.be.domain.like.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_like", uniqueConstraints = {
        @UniqueConstraint(name = "unique_like", columnNames = {"post_id", "user_id"})
})
@Getter
@NoArgsConstructor
public class PostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;
}
