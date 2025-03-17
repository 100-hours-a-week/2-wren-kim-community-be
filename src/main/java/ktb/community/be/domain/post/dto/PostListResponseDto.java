package ktb.community.be.domain.post.dto;

import ktb.community.be.domain.post.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostListResponseDto {
    private final Long id;
    private final String title;
    private final String memberNickname;
    private final int likeCount;
    private final int commentCount;
    private final int viewCount;
    private final LocalDateTime createdAt;

    public PostListResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.memberNickname = post.getMember().getNickname();
        this.likeCount = post.getLikes().size();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
    }
}
