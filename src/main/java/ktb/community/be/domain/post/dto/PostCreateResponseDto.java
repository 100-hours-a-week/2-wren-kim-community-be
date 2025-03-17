package ktb.community.be.domain.post.dto;

import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostCreateResponseDto {

    private Long id;
    private String title;
    private String content;
    private String memberNickname;
    private List<String> imageUrls;

    public PostCreateResponseDto(Post post, List<PostImage> images) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.memberNickname = post.getMember().getNickname();
        this.imageUrls = images.stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
