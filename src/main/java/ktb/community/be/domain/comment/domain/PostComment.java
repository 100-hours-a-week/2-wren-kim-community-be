package ktb.community.be.domain.comment.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post_comment")
@SQLDelete(sql = "UPDATE post_comment SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@NoArgsConstructor
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean isDeleted = false;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TIMESTAMP NULL DEFAULT NULL")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<PostComment> replies = new ArrayList<>();

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
