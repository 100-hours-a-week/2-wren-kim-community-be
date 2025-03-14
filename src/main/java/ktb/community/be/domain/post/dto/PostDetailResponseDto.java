package ktb.community.be.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@JsonPropertyOrder({"id", "title", "content", "viewCount", "likeCount", "commentCount", "userNickname", "userProfileImageUrl", "imageUrls", "comments"})
@Getter
public class PostDetailResponseDto {

    private Long id;
    private String title;
    private String content;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private String userNickname;
    private String userProfileImageUrl;
    private List<String> imageUrls;
    private List<CommentResponseDto> comments;

    public PostDetailResponseDto(Post post, int likeCount, List<PostImage> images, List<CommentResponseDto> comments) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.viewCount = post.getViewCount();
        this.likeCount = likeCount; // 직접 조회한 값 사용
        this.commentCount = post.getCommentCount();
        this.userNickname = post.getUser().getNickname();
        this.userProfileImageUrl = post.getUser().getProfileImageUrl();
        this.imageUrls = images.stream().map(PostImage::getImageUrl).collect(Collectors.toList());
        this.comments = comments;
    }
}
