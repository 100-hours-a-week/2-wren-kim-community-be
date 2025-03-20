package ktb.community.be.global.config;

import ktb.community.be.global.security.JwtAccessDeniedHandler;
import ktb.community.be.global.security.JwtAuthenticationEntryPoint;
import ktb.community.be.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // pring Security의 기본 CORS 설정 비활성화
                .cors(cors -> cors.disable())

                // CSRF 보호 비활성화
                .csrf(csrf -> csrf.disable())

                // CORS 필터 적용 (JWT 인증 필터보다 먼저 실행)
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT 예외 처리 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // 세션을 사용하지 않음 (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인증 및 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/actuator/**"
                        ).permitAll() // Swagger 관련 요청 허용
                        .requestMatchers("/auth/**").permitAll() // 로그인, 회원가입 허용
                        .requestMatchers("/api/**").permitAll() // 기타 API 허용 (보안 설정 필요하면 변경 가능)
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )

                // 기본 로그인 및 HTTP Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                // JWT 필터 적용
                .apply(new JwtSecurityConfig(tokenProvider));
//                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
