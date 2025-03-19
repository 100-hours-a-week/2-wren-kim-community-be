package ktb.community.be.domain.post.dto;

import ktb.community.be.domain.post.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostListResponseDto {
    private final Long id;
    private final String title;
    private final String memberNickname;
    private final int likeCount;
    private final int commentCount;
    private final int viewCount;
    private final LocalDateTime createdAt;

    public static PostListResponseDto from(Post post) {
        return PostListResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .memberNickname(post.getMember().getNickname() != null ? post.getMember().getNickname() : "(알수없음)")
                .likeCount(post.getLikes().size())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
