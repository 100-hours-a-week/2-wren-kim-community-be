package ktb.community.be.domain.post.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.image.domain.PostImage;
import ktb.community.be.domain.like.domain.PostLike;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "post")
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW(), is_deleted = 1 WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true) // Member 삭제 시 게시글 유지
    private Member member;

    @Column(length = 255, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer viewCount = 0;

    @Formula("(SELECT COUNT(pl.id) FROM post_like pl WHERE pl.post_id = id AND pl.is_deleted = 0)")
    private int likeCount;

    @Column(columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer commentCount = 0;

    @Builder.Default
    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @OrderBy("orderIndex ASC")
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<PostComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostLike> likes = new HashSet<>();

    public void increaseViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateCommentCount(int count) {
        this.commentCount = count;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
