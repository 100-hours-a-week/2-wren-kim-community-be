package ktb.community.be.global.scheduler;

import ktb.community.be.domain.member.application.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberService memberService;

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 실행
    public void cleanupDeletedMembers() {
        log.info("* 30일 지난 탈퇴 회원 이메일/닉네임 변경 작업 시작");
        try {
            memberService.processExpiredDeletedAccounts();
            log.info("* 30일 지난 탈퇴 회원 처리 완료");
        } catch (Exception e) {
            log.error("* 30일 지난 탈퇴 회원 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
