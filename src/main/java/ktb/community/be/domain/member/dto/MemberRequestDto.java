package ktb.community.be.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ktb.community.be.domain.member.domain.Authority;
import ktb.community.be.domain.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestDto {

    @NotBlank(message = "*이메일을 입력해주세요.")
    @Email(message = "*올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)")
    private String email;

    @NotBlank(message = "*비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "*비밀번호는 8자 이상, 20자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "*비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "*비밀번호를 한 번 더 입력해주세요.")
    private String confirmPassword;

    @NotBlank(message = "*닉네임을 입력해주세요.")
    @Size(max = 10, message = "*닉네임은 최대 10자까지 작성 가능합니다.")
    @Pattern(regexp = "^[^\\s]+$", message = "*띄어쓰기를 없애주세요.")
    private String nickname;

    private MultipartFile profileImage;

    /**
     * Member 엔티티로 변환
     */
    public Member toMember(String encodedPassword, String imageUrl) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImageUrl(imageUrl)
                .authority(Authority.ROLE_USER)
                .isDeleted(false)
                .build();
    }

    /**
     * 로그인 인증 객체 생성
     */
    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
    }
}
