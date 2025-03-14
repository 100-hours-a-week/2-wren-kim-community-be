package ktb.community.be.domain.post.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.like.domain.PostLike;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // ✅ 좋아요 수를 @Formula로 조회 (별도 쿼리 실행 방지)
    @Formula("(SELECT COUNT(pl.id) FROM post_like pl WHERE pl.post_id = id AND pl.is_deleted = 0)")
    private int likeCount;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer commentCount = 0;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10) // 이미지 10개씩 한 번에 가져옴
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10) // 댓글 10개씩 한 번에 가져옴
    private List<PostComment> comments = new ArrayList<>();

    public Post(User author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }
}
