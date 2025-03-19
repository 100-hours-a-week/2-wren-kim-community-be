package ktb.community.be.domain.post.dto;

import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostCreateResponseDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String memberNickname;
    private final List<String> imageUrls;

    public static PostCreateResponseDto from(Post post, List<PostImage> images) {
        return PostCreateResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .memberNickname(post.getMember().getNickname() != null ? post.getMember().getNickname() : "(알수없음)")
                .imageUrls(images != null ? images.stream().map(PostImage::getImageUrl).collect(Collectors.toList()) : List.of())
                .build();
    }
}
