package ktb.community.be.domain.post.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_image")
@Getter
@NoArgsConstructor
public class PostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 이미지 업로드한 유저 관계 추가
    private User user;

    @Column(length = 512, nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0", nullable = false)
    private Integer orderIndex;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    public PostImage(Post post, User user, String imageUrl, Integer orderIndex) {
        this.post = post;
        this.user = user;
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
