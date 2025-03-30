package ktb.community.be.domain.image.dto;

import ktb.community.be.domain.image.domain.PostImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostImageDto {
    private Long id;
    private String imageUrl;
    private int orderIndex;

    public static PostImageDto from(PostImage image) {
        return PostImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .orderIndex(image.getOrderIndex())
                .build();
    }
}
