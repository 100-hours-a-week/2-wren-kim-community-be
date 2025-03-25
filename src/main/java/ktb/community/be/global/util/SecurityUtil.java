package ktb.community.be.global.util;

import ktb.community.be.domain.member.dao.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class SecurityUtil {

    private static MemberRepository memberRepository;

    public static void setMemberRepository(MemberRepository repo) {
        SecurityUtil.memberRepository = repo;
    }

    // SecurityContext 에 유저 정보가 저장되는 시점
    // Request 가 들어올 때 JwtFilter 의 doFilter 에서 저장
    public static Long getCurrentMemberId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
            || authentication.getName() == null
            || authentication.getName().equals("anonymousUser")) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");  // 401 Unauthorized 처리
        }

        try {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {

                Long memberId = Long.valueOf(userDetails.getUsername());

                return memberRepository.findById(memberId)
                        .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("존재하지 않는 사용자입니다."))
                        .getId();
            } else {
                throw new AuthenticationCredentialsNotFoundException("잘못된 인증 정보입니다.");
            }
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("인증 정보 처리 중 오류가 발생했습니다.");
        }
    }
}
