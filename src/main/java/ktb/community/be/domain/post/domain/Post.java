package ktb.community.be.domain.post.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@NoArgsConstructor
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = true) // User 삭제 시 게시글 유지
    private User author;

    @Column(length = 255, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer viewCount = 0;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer likeCount = 0;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer commentCount = 0;

    private LocalDateTime deletedAt;
}
