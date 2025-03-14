package ktb.community.be.domain.comment.dto;

import ktb.community.be.domain.comment.domain.PostComment;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommentResponseDto {
    private Long id;
    private String content;
    private String authorNickname;
    private String authorProfileImageUrl;
    private List<CommentResponseDto> replies = new ArrayList<>();

    public CommentResponseDto(PostComment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorNickname = comment.getUser().getNickname();
        this.authorProfileImageUrl = comment.getUser().getProfileImageUrl();
    }
}
