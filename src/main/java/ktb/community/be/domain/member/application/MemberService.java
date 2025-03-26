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

import java.time.LocalDateTime;
import java.util.List;

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
    @Transactional
    public MemberResponseDto updateMemberInfo(Long memberId, String nickname, MultipartFile profileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "*사용자를 찾을 수 없습니다."));

        if (nickname != null && !nickname.trim().isEmpty()) {
            validateNickname(nickname);
            member.updateNickname(nickname);
        } else {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*닉네임을 입력해주세요.");
        }

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

    /**
     * 회원 탈퇴 (소프트 삭제)
     */
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (member.getIsDeleted()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 탈퇴한 계정입니다.");
        }

        member.softDelete();
    }

    /**
     * 로그인 시 탈퇴한 회원인지 확인하고,
     * 탈퇴 후 30일 이내면 복구 처리 진행
     */
    @Transactional
    public void restoreIfPossible(String email) {
        List<Member> candidates = memberRepository.findAllByEmailIncludingDeleted(email);

        if (candidates.isEmpty()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다.");
        }

        // 1순위: 삭제 안 된 계정이 있으면 복구 필요 없음
        for (Member m : candidates) {
            if (!m.getIsDeleted()) return;
        }

        // 복구 가능 후보 찾기 (deletedAt + 30초 이내)
        Member restorable = candidates.stream()
                .filter(Member::getIsDeleted)
                .filter(m -> {
                    LocalDateTime deletedAt = m.getDeletedAt();
                    return deletedAt != null && deletedAt.plusDays(30).isAfter(LocalDateTime.now());
                })
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "계정이 완전히 삭제되었습니다. 새로 가입해주세요."));

        // 원래 닉네임/이메일 추출
        String originalEmail = extractOriginalEmail(restorable.getEmail());
        String originalNickname = extractOriginalNickname(restorable.getNickname());

        // 중복 체크 (자기 자신 제외)
        if (memberRepository.existsByEmail(originalEmail) && !originalEmail.equals(restorable.getEmail())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*복구할 수 없습니다. 이미 사용 중인 이메일입니다.");
        }

        if (memberRepository.existsByNickname(originalNickname) && !originalNickname.equals(restorable.getNickname())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "*복구할 수 없습니다. 이미 사용 중인 닉네임입니다.");
        }

        // 복구 처리
        restorable.restoreAccount();
        restorable.updateEmail(originalEmail);
        restorable.updateNickname(originalNickname);
    }

    private String extractOriginalEmail(String deletedEmail) {
        if (!deletedEmail.startsWith("deleted_")) return deletedEmail;
        int prefixLength = "deleted_".length();
        int lastUnderscore = deletedEmail.lastIndexOf('_');
        if (lastUnderscore > prefixLength) {
            return deletedEmail.substring(prefixLength, lastUnderscore);
        }
        return deletedEmail;
    }

    private String extractOriginalNickname(String deletedNickname) {
        if (!deletedNickname.startsWith("deleted_")) return deletedNickname;

        String withoutPrefix = deletedNickname.substring("deleted_".length());
        int lastUnderscore = withoutPrefix.lastIndexOf('_');
        if (lastUnderscore > 0) {
            return withoutPrefix.substring(0, lastUnderscore);
        }
        return withoutPrefix;
    }

    /**
     * 30일이 지난 탈퇴 회원의 이메일/닉네임을 deleted_로 변경
     */
    @Transactional(readOnly = false)
    public void processExpiredDeletedAccounts() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        List<Member> expiredMembers = memberRepository.findExpiredAndNotAlreadyMarked(thresholdDate);

        System.out.println("만료된 회원 수: " + expiredMembers.size());

        for (Member member : expiredMembers) {
            System.out.println("삭제 처리 대상: " + member.getEmail());
            member.markAsDeleted();
        }
    }
}
