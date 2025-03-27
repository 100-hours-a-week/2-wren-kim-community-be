package ktb.community.be.domain.post.dto;

import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.member.domain.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequestDto {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 26, message = "제목은 최대 26자까지 가능합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    public Post toEntity(Member member) {
        return Post.builder()
                .member(member)
                .title(title)
                .content(content)
                .viewCount(0)  // 기본값 명시적으로 설정
                .commentCount(0)
                .isDeleted(false)
                .build();
    }
}
