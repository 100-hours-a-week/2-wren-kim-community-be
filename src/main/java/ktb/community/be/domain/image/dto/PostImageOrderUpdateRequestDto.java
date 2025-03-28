package ktb.community.be.domain.image.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class PostImageOrderUpdateRequestDto {
    // key: 이미지 ID, value: 새로운 순서
    private Map<Long, Integer> orderIndexMap;
}
