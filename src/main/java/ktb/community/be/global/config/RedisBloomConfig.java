package ktb.community.be.global.config;

import io.rebloom.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisBloomConfig {

    @Bean
    public Client bloomClient() {
        return new Client("localhost", 6379); // Docker RedisBloom 포트
    }
}
