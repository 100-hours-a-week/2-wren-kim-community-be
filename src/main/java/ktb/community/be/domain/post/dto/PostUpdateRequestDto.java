package ktb.community.be.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class PostUpdateRequestDto {
    private String title;
    private String content;
    private List<Long> keepImageIds; // 유지할 이미지 ID 리스트
    private Map<Long, Integer> orderIndexMap; // 이미지 ID → orderIndex 매핑

    // orderIndexMap이 비어 있는 경우 예외 처리
    public boolean hasOrderIndexUpdate() {
        return orderIndexMap != null && !orderIndexMap.isEmpty();
    }
}
