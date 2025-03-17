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
        return new MemberResponseDto(member.getEmail(), member.getNickname(), member.getProfileImageUrl());
    }
}
