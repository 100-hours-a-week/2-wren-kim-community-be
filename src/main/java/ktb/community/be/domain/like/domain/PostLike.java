package ktb.community.be.domain.like.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_like", uniqueConstraints = {
        @UniqueConstraint(name = "unique_like", columnNames = {"post_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = true)
    private SoftDeleteType softDeleteType;

    @PrePersist
    public void prePersist() {
        this.isDeleted = (this.isDeleted == null) ? false : this.isDeleted;
        this.createdAt = (this.createdAt == null) ? LocalDateTime.now() : this.createdAt;
    }

    // 사용자가 직접 좋아요 취소
    public void softDeleteByUser() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.softDeleteType = SoftDeleteType.USER_ACTION;
    }

    // 게시글 삭제로 인해 Soft Delete
    public void softDeleteByPostDeletion() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.softDeleteType = SoftDeleteType.POST_DELETION;
    }

    // 좋아요 복구 (단순 취소된 경우만 복구 가능)
    public void restore() {
        if (this.softDeleteType == SoftDeleteType.USER_ACTION) {
            this.isDeleted = false;
            this.deletedAt = null;
            this.updatedAt = LocalDateTime.now();
            this.softDeleteType = null;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
