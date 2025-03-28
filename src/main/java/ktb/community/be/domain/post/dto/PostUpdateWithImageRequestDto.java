package ktb.community.be.domain.post.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateWithImageRequestDto {

    @Valid
    private PostUpdateRequestDto postData; // 기존 제목, 내용, 이미지 순서 정보 등
    private List<MultipartFile> newImages;
    private List<Integer> orderIndexes;
}
