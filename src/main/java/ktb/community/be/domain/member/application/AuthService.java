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
     */
    @Transactional
    public TokenDto login(LoginRequestDto loginRequestDto) {
        Member member = getValidMemberByEmail(loginRequestDto.getEmail());
        validatePassword(loginRequestDto.getPassword(), member.getPassword());
        return generateAndSaveTokens(member, loginRequestDto.getPassword());
    }

    private Member getValidMemberByEmail(String email) {
        List<Member> candidates = memberRepository.findAllByEmailIncludingDeleted(email);
        if (candidates.isEmpty()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다.");
        }

        Member member = candidates.stream()
                .filter(m -> !m.getIsDeleted())
                .findFirst()
                .orElse(candidates.get(0));

        if (member.getIsDeleted()) {
            memberService.restoreIfPossible(email);
            return memberRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "복구된 회원을 찾을 수 없습니다."));
        }

        return member;
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "비밀번호가 일치하지 않습니다.");
        }
    }

    private TokenDto generateAndSaveTokens(Member member, String rawPassword) {
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(new UsernamePasswordAuthenticationToken(member.getEmail(), rawPassword));

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication, member.getId());

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
        String accessToken = tokenRequestDto.getAccessToken();
        String refreshToken = tokenRequestDto.getRefreshToken();

        // 1. Refresh Token 유효성 검증
        validateRefreshToken(refreshToken);

        // 2. Access Token 에서 Member ID 추출
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        String memberId = authentication.getName();

        // 3. accessToken vs refreshToken 의 subject 일치 여부 확인
        validateTokenSubjectsMatch(memberId, refreshToken, accessToken);

        // 4. 저장된 Refresh Token 조회
        RefreshToken savedRefreshToken = getSavedRefreshTokenOrThrow(memberId);

        // 5. 저장된 Refresh Token 과 요청된 Refresh Token 비교
        checkRefreshTokenMatches(savedRefreshToken, refreshToken, memberId, accessToken);

        // 6. 회원 정보 조회
        Member member = getMemberOrThrow(memberId);

        // 7. 새로운 토큰 발급 및 저장
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication, member.getId());
        RefreshToken newRefreshToken = savedRefreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        return tokenDto;
    }

    private void validateRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 Refresh Token입니다.");
        }
    }

    private void validateTokenSubjectsMatch(String memberId, String refreshToken, String accessToken) {
        String refreshSubject = tokenProvider.getSubject(refreshToken);
        if (!memberId.equals(refreshSubject)) {
            log.warn("[토큰 위조 의심] accessToken의 유저ID: {}, refreshToken의 subject: {}", memberId, refreshSubject);
            throw new CustomException(ErrorCode.INVALID_REQUEST, "토큰의 유저 정보가 일치하지 않습니다.");
        }
    }

    private RefreshToken getSavedRefreshTokenOrThrow(String memberId) {
        return refreshTokenRepository.findByKey(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "로그아웃된 사용자입니다."));
    }

    private void checkRefreshTokenMatches(RefreshToken savedToken, String incomingToken, String memberId, String accessToken) {
        if (!savedToken.getValue().equals(incomingToken)) {
            log.warn("[토큰 위조 의심] memberId: {}, accessToken은 {}, refreshToken은 DB에 존재하지 않음 또는 일치하지 않음",
                    memberId, accessToken);
            throw new CustomException(ErrorCode.INVALID_REQUEST, "토큰의 유저 정보가 일치하지 않습니다.");
        }
    }

    private Member getMemberOrThrow(String memberId) {
        return memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다."));
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
