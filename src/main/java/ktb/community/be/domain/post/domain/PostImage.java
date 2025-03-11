package ktb.community.be.domain.post.domain;

import jakarta.persistence.*;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(length = 512, nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;
}
