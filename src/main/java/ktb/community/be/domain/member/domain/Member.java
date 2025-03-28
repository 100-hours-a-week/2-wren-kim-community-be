package ktb.community.be.domain.member.domain;

import jakarta.persistence.*;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.like.domain.PostLike;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.image.domain.PostImage;
import ktb.community.be.global.domain.BaseTimeEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = "email"),
        @UniqueConstraint(name = "unique_nickname", columnNames = "nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false, unique = true)
    private String email;

    @Column(length = 512, nullable = false)
    private String password;

    @Column(length = 30, nullable = false, unique = true)
    private String nickname;

    @Column(length = 512, nullable = false)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> postComments;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImages;

    @Builder
    public Member(String email, String password, String nickname, String profileImageUrl, Authority authority) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.authority = authority;
    }

    @PrePersist
    public void prePersist() {
        this.isActive = this.isActive == null || this.isActive;
        this.isDeleted = this.isDeleted != null && this.isDeleted;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        if (!this.email.startsWith("deleted_")) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String randomSuffix = UUID.randomUUID().toString().substring(0, 6);
            this.email = String.format("deleted_%s_%s", this.email, randomSuffix);
        }

        if (!this.nickname.startsWith("deleted_")) {
            String randomSuffix = UUID.randomUUID().toString().substring(0, 6);
            this.nickname = String.format("deleted_%s_%s", this.nickname, randomSuffix);
        }
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void restoreAccount() {
        this.isDeleted = false;
        this.isActive = true;
        this.deletedAt = null;
    }

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updateProfileImage(String newProfileImageUrl) {
        this.profileImageUrl = newProfileImageUrl;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
