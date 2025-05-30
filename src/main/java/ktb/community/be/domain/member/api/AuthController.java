package ktb.community.be.domain.member.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ktb.community.be.domain.member.application.AuthService;
import ktb.community.be.domain.member.dto.LoginRequestDto;
import ktb.community.be.domain.member.dto.MemberRequestDto;
import ktb.community.be.domain.member.dto.MemberResponseDto;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.security.TokenDto;
import ktb.community.be.global.security.TokenRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 프로필 이미지를 입력하여 회원가입을 진행합니다.")
    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<MemberResponseDto>> signup(
            @Valid @ModelAttribute MemberRequestDto memberRequestDto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new CustomException(ErrorCode.INVALID_REQUEST, errorMessage);
        }

        MemberResponseDto responseDto = authService.signup(memberRequestDto);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", responseDto));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력하여 로그인을 진행하고 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        TokenDto tokenDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(ApiResponse.success("로그인이 성공적으로 완료되었습니다.", tokenDto));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenDto>> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        TokenDto tokenDto = authService.reissue(tokenRequestDto);
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", tokenDto));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 Refresh Token을 삭제하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody TokenRequestDto tokenRequestDto
    ) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        tokenRequestDto.setAccessToken(accessToken);
        authService.logout(tokenRequestDto);
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다."));
    }
}
