package ktb.community.be.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {

    private Long memberId;

    @NotNull(message = "내용을 입력해주세요.")
    @NotBlank(message = "내용은 공백일 수 없습니다.")
    private String content;
}
