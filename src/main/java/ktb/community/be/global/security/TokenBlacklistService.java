package ktb.community.be.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    /**
     * accessToken을 블랙리스트에 등록 (TTL 기반)
     */
    public void blacklistAccessToken(String accessToken, long expirationTime) {
        String key = "blacklist:" + accessToken;

        redisTemplate.opsForValue().set(key, "true", expirationTime, TimeUnit.MILLISECONDS);
        log.info("AccessToken 블랙리스트 등록 완료: {} ({}ms)", accessToken, expirationTime);
    }

    /**
     * 블랙리스트 여부 조회 (TTL 키만으로 확인)
     */
    public boolean isBlacklisted(String accessToken) {
        String key = "blacklist:" + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
