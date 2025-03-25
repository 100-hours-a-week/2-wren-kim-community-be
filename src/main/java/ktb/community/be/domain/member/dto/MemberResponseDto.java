package ktb.community.be.domain.member.dto;

import ktb.community.be.domain.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {

    private String email;
    private String nickname;
    private String profileImageUrl;

    public static MemberResponseDto of(Member member) {
        // 만약 닉네임이 deleted_로 시작하면 제거한 후 반환
        String fixedNickname = member.getNickname().startsWith("deleted_") ? member.getNickname().replace("deleted_", "") : member.getNickname();

        return new MemberResponseDto(member.getEmail(), fixedNickname, member.getProfileImageUrl());
    }
}
