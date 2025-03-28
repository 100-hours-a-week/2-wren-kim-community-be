package ktb.community.be.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.image.domain.PostImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@JsonPropertyOrder({"id", "title", "content", "createdAt", "updatedAt", "viewCount", "likeCount", "commentCount", "memberNickname", "memberProfileImageUrl", "imageUrls", "comments"})
@Getter
@Builder
public class PostDetailResponseDto {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private String memberNickname;
    private String memberProfileImageUrl;
    private List<String> imageUrls;
    private List<CommentResponseDto> comments;

    public static PostDetailResponseDto from(Post post, int likeCount, List<PostImage> images, List<CommentResponseDto> comments) {
        String nickname = "(알수없음)";
        String profileImageUrl = null;

        if (post.getMember() != null && !post.getMember().getIsDeleted()) {
            nickname = post.getMember().getNickname();
            profileImageUrl = post.getMember().getProfileImageUrl();
        }

        return PostDetailResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(post.getCommentCount())
                .memberNickname(nickname)
                .memberProfileImageUrl(profileImageUrl)
                .imageUrls(images.stream().map(PostImage::getImageUrl).collect(Collectors.toList()))
                .comments(comments)
                .build();
    }
}
