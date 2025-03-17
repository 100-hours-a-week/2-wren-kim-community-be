package ktb.community.be.global.security;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenRequestDto {

    private String accessToken;
    private String refreshToken;
}
