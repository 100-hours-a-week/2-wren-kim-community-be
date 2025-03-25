package ktb.community.be.domain.member.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ktb.community.be.domain.member.application.MemberService;
import ktb.community.be.domain.member.dto.MemberResponseDto;
import ktb.community.be.domain.member.dto.PasswordUpdateRequestDto;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDto>> findMemberInfoById() {
        MemberResponseDto responseDto = memberService.findMemberInfoById(securityUtil.getCurrentMemberId());
        return ResponseEntity.ok(ApiResponse.success("회원 정보를 조회하였습니다.", responseDto));
    }

    @Operation(summary = "이메일로 회원 조회", description = "이메일을 통해 특정 회원의 정보를 조회합니다.")
    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> findMemberInfoByEmail(@PathVariable String email) {
        MemberResponseDto responseDto = memberService.findMemberInfoByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("회원 정보를 조회하였습니다.", responseDto));
    }

    @Operation(summary = "회원 정보 수정", description = "닉네임과 프로필 이미지를 수정합니다.")
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<MemberResponseDto>> updateMemberInfo(
            @RequestPart(value = "nickname", required = false) String nickname,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        MemberResponseDto updatedMember = memberService.updateMemberInfo(securityUtil.getCurrentMemberId(), nickname, profileImage);
        return ResponseEntity.ok(ApiResponse.success("회원 정보가 수정되었습니다.", updatedMember));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@Valid @RequestBody PasswordUpdateRequestDto passwordUpdateRequestDto) {
        memberService.updatePassword(securityUtil.getCurrentMemberId(), passwordUpdateRequestDto);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 비활성화합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMember() {
        memberService.deleteMember(securityUtil.getCurrentMemberId());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }
}
