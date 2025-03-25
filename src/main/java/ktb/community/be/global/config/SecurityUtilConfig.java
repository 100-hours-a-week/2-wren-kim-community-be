package ktb.community.be.global.config;

import jakarta.annotation.PostConstruct;
import ktb.community.be.domain.member.dao.MemberRepository;
import ktb.community.be.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SecurityUtilConfig {

    private final MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        SecurityUtil.setMemberRepository(memberRepository);
    }
}
