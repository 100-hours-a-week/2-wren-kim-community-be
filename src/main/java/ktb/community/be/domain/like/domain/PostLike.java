package ktb.community.be.domain.like.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_like", uniqueConstraints = {
        @UniqueConstraint(name = "unique_like", columnNames = {"post_id", "member_id"})
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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder.Default
    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = true)
    private SoftDeleteType softDeleteType;

    @PrePersist
    public void prePersist() {
        this.isDeleted = this.isDeleted != null && this.isDeleted;
    }

    // 사용자가 직접 좋아요 취소
    public void softDeleteByMember() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.softDeleteType = SoftDeleteType.MEMBER_ACTION;
    }

    // 게시글 삭제로 인해 Soft Delete
    public void softDeleteByPostDeletion() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.softDeleteType = SoftDeleteType.POST_DELETION;
    }

    // 좋아요 복구 (단순 취소된 경우만 복구 가능)
    public void restore() {
        if (this.softDeleteType == SoftDeleteType.MEMBER_ACTION) {
            this.isDeleted = false;
            this.deletedAt = null;
            this.softDeleteType = null;
        }
    }
}
