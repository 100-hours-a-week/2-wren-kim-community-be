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

    public void blacklistAccessToken(String accessToken, long expirationTime) {
        String key = "blacklist:" + accessToken;
        redisTemplate.opsForValue().set(key, "true", expirationTime, TimeUnit.MILLISECONDS);
        log.info("Redis에 블랙리스트 등록: {} ({}ms)", key, expirationTime);
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + accessToken));
    }
}
