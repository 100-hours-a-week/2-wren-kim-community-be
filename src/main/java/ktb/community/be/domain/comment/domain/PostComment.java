package ktb.community.be.domain.comment.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post_comment")
@SQLDelete(sql = "UPDATE post_comment SET deleted_at = NOW() WHERE id = ?")
//@Where(clause = "deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean isDeleted = false;

    @Column(columnDefinition = "TIMESTAMP NULL DEFAULT NULL")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<PostComment> replies = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.isDeleted = this.isDeleted != null && this.isDeleted;
        this.replies = this.replies == null ? new ArrayList<>() : this.replies;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
