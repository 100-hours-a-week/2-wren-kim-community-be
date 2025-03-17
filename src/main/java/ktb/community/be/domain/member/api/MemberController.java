package ktb.community.be.domain.member.api;

import io.swagger.v3.oas.annotations.Operation;
import ktb.community.be.domain.member.application.MemberService;
import ktb.community.be.domain.member.dto.MemberResponseDto;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDto>> findMemberInfoById() {
        MemberResponseDto responseDto = memberService.findMemberInfoById(SecurityUtil.getCurrentMemberId());
        return ResponseEntity.ok(ApiResponse.success("회원 정보를 조회하였습니다.", responseDto));
    }

    @Operation(summary = "이메일로 회원 조회", description = "이메일을 통해 특정 회원의 정보를 조회합니다.")
    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> findMemberInfoByEmail(@PathVariable String email) {
        MemberResponseDto responseDto = memberService.findMemberInfoByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("회원 정보를 조회하였습니다.", responseDto));
    }
}
