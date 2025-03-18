package ktb.community.be.domain.member.application;

import ktb.community.be.domain.member.dao.MemberRepository;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.domain.member.dto.MemberResponseDto;
import ktb.community.be.domain.member.dto.PasswordUpdateRequestDto;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    public MemberResponseDto findMemberInfoById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));
    }

    public MemberResponseDto findMemberInfoByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberResponseDto::of)
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
    }

    /**
     * 회원 정보 수정
     */
    public MemberResponseDto updateMemberInfo(Long memberId, String nickname, MultipartFile profileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "*사용자를 찾을 수 없습니다."));

        // 닉네임 검증 및 업데이트
        if (nickname != null && !nickname.trim().isEmpty()) { // 빈 문자열 체크 추가
            validateNickname(nickname);
            member.updateNickname(nickname);
        } else {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*닉네임을 입력해주세요.");
        }

        // 프로필 이미지 저장 및 업데이트
        if (profileImage != null && !profileImage.isEmpty()) {
            String newProfileImagePath = fileStorageService.storeProfileImage(profileImage);
            member.updateProfileImage(newProfileImagePath);
        }

        return MemberResponseDto.of(member);
    }

    private void validateNickname(String nickname) {
        if (nickname.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*닉네임을 입력해주세요.");
        }

        if (nickname.length() > 10) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*닉네임은 최대 10자까지 작성 가능합니다.");
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*중복된 닉네임입니다.");
        }
    }

    /**
     * 비밀번호 수정
     */
    @Transactional
    public void updatePassword(Long memberId, PasswordUpdateRequestDto passwordUpdateRequestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "*사용자를 찾을 수 없습니다."));

        validatePassword(passwordUpdateRequestDto.getNewPassword(), passwordUpdateRequestDto.getConfirmPassword());

        // 비밀번호 암호화 후 업데이트
        String encryptedPassword = passwordEncoder.encode(passwordUpdateRequestDto.getNewPassword());
        member.updatePassword(encryptedPassword);
    }

    private void validatePassword(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*비밀번호 확인과 다릅니다.");
        }
    }
}
