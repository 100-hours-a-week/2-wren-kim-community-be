package ktb.community.be.domain.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {
    private Long userId;
    private String content;
}
