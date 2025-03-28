package ktb.community.be.domain.image.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // 이미지 업로드한 유저 관계 추가
    private Member member;

    @Column(length = 512, nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0", nullable = false)
    private Integer orderIndex;

    @Builder.Default
    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.isDeleted = this.isDeleted != null && this.isDeleted;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateOrderIndex(int newOrderIndex) {
        if (!this.orderIndex.equals(newOrderIndex)) {
            this.orderIndex = newOrderIndex;
        }
    }
}
