package ktb.community.be.domain.member.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import ktb.community.be.domain.member.dao.MemberRepository;
import ktb.community.be.domain.member.dao.RefreshTokenRepository;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.domain.member.domain.RefreshToken;
import ktb.community.be.domain.member.dto.LoginRequestDto;
import ktb.community.be.domain.member.dto.MemberRequestDto;
import ktb.community.be.domain.member.dto.MemberResponseDto;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.security.TokenBlacklistService;
import ktb.community.be.global.util.FileStorageService;
import ktb.community.be.global.security.TokenDto;
import ktb.community.be.global.security.TokenProvider;
import ktb.community.be.global.security.TokenRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileStorageService fileStorageService;
    private final MemberService memberService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 회원가입
     */
    @Transactional
    public MemberResponseDto signup(MemberRequestDto memberRequestDto) {
        // 1. 이메일 중복 검사
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*중복된 이메일입니다.");
        }

        // 2. 닉네임 중복 검사
        if (memberRepository.existsByNickname(memberRequestDto.getNickname())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*중복된 닉네임입니다.");
        }

        // 3. 비밀번호 확인 검사
        if (!memberRequestDto.getPassword().equals(memberRequestDto.getConfirmPassword())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*비밀번호가 다릅니다.");
        }

        // 4. 프로필 이미지 필수 검사
        if (memberRequestDto.getProfileImage() == null || memberRequestDto.getProfileImage().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*프로필 사진을 추가해주세요.");
        }

        // 5. 프로필 이미지 저장
        String imageUrl = fileStorageService.storeProfileImage(memberRequestDto.getProfileImage());

        // 6. 비밀번호 암호화 후 회원 저장
        String encodedPassword = passwordEncoder.encode(memberRequestDto.getPassword());
        Member member = memberRequestDto.toMember(encodedPassword, imageUrl);

        return MemberResponseDto.of(memberRepository.save(member));
    }

    /**
     * 로그인
     * (참고: 위 로그인 메서드는 탈퇴 회원 처리가 되어 있지 않음)
     */
    @Transactional
    public TokenDto login(LoginRequestDto loginRequestDto) {
        // 1. 이메일 기반 조회
        List<Member> candidates = memberRepository.findAllByEmailIncludingDeleted(loginRequestDto.getEmail());
        if (candidates.isEmpty()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다.");
        }

        Member member = candidates.stream()
                .filter(m -> !m.getIsDeleted())
                .findFirst()
                .orElse(candidates.get(0)); // 삭제된 경우 복구 시도 후 사용

        // 2. 탈퇴 복구 시도
        if (member.getIsDeleted()) {
            try {
                memberService.restoreIfPossible(member.getEmail());
            } catch (CustomException e) {
                throw e; // GlobalExceptionHandler에서 처리되도록 던짐
            }

            member = memberRepository.findByEmail(member.getEmail())
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "복구된 회원을 찾을 수 없습니다."));
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "비밀번호가 일치하지 않습니다.");
        }

        // 4. 인증 토큰 생성 및 인증 처리
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(member.getEmail(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 5. 토큰 발급
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication, member.getId());

        // 6. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(member.getId().toString())
                .value(tokenDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);

        return tokenDto;
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 Refresh Token입니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장된 Refresh Token 가져오기
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "로그아웃된 사용자입니다."));

        // 4. Refresh Token 일치 여부 확인
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 회원 정보 가져오기
        Member member = memberRepository.findById(Long.parseLong(authentication.getName()))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다."));

        // 6. 새로운 JWT 생성 (memberId 추가)
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication, member.getId());

        // 7. Refresh Token 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        return tokenDto;
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 Refresh Token입니다.");
        }

        // 2. 현재 로그인한 사용자의 Authentication 정보 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. Refresh Token이 존재하는지 확인하고 삭제
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "이미 로그아웃된 사용자입니다."));

        refreshTokenRepository.delete(refreshToken);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(tokenProvider.getKey())
                .build()
                .parseClaimsJws(tokenRequestDto.getAccessToken())
                .getBody();

        long now = System.currentTimeMillis();
        long exp = claims.getExpiration().getTime();
        long remainingTime = exp - now;
        if (remainingTime <= 0) {
            log.warn("토큰의 남은 시간이 0 이하라 블랙리스트에 저장하지 않음");
            return;
        }

        tokenBlacklistService.blacklistAccessToken(tokenRequestDto.getAccessToken(), remainingTime);

        String accessToken = tokenRequestDto.getAccessToken();
        tokenBlacklistService.blacklistAccessToken(accessToken, remainingTime);
    }
}
