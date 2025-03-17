package ktb.community.be.domain.member.api;

import io.swagger.v3.oas.annotations.Operation;
import ktb.community.be.domain.member.application.AuthService;
import ktb.community.be.domain.member.dto.MemberRequestDto;
import ktb.community.be.domain.member.dto.MemberResponseDto;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.security.TokenDto;
import ktb.community.be.global.security.TokenRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 입력하여 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponseDto>> signup(@RequestBody MemberRequestDto memberRequestDto) {
        MemberResponseDto responseDto = authService.signup(memberRequestDto);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", responseDto));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력하여 로그인을 진행하고 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> login(@RequestBody MemberRequestDto memberRequestDto) {
        TokenDto tokenDto = authService.login(memberRequestDto);
        return ResponseEntity.ok(ApiResponse.success("로그인이 성공적으로 완료되었습니다.", tokenDto));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenDto>> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        TokenDto tokenDto = authService.reissue(tokenRequestDto);
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", tokenDto));
    }
}
