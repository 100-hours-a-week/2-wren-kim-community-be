package ktb.community.be.domain.user.domain;

import jakarta.persistence.*;
import ktb.community.be.global.domain.BaseTimeEntity;

@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = "email"),
        @UniqueConstraint(name = "unique_nickname", columnNames = "nickname")
})
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    private String email;

    @Column(length = 60, nullable = false)
    private String password;

    @Column(length = 30, nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive = true;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;
}
