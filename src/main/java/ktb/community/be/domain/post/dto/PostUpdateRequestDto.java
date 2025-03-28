package ktb.community.be.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequestDto {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 26, message = "제목은 최대 26자까지 가능합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
    private List<Long> keepImageIds; // 유지할 이미지 ID 리스트
    private Map<Long, Integer> orderIndexMap; // 이미지 ID → orderIndex 매핑

    // orderIndexMap이 비어 있는 경우 예외 처리
    public boolean hasOrderIndexUpdate() {
        return orderIndexMap != null && !orderIndexMap.isEmpty();
    }
}
