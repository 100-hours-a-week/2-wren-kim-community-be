package ktb.community.be.domain.image.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import ktb.community.be.domain.image.application.PostImageService;
import ktb.community.be.domain.image.dto.PostImageOrderUpdateRequestDto;
import ktb.community.be.domain.post.application.PostService;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class PostImageController {

    private final PostService postService;
    private final PostImageService postImageService;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;

    @Operation(summary = "게시글 이미지 업로드", description = "게시글 작성 후 이미지를 업로드합니다.")
    @PostMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> uploadImages(
            @PathVariable Long postId,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart("orderIndexes") String orderIndexesJson
    ) throws JsonProcessingException {

        Long memberId = securityUtil.getCurrentMemberId();
        List<Integer> orderIndexes = objectMapper.readValue(orderIndexesJson, new TypeReference<>() {});

        postService.uploadImages(postId, memberId, images, orderIndexes);
        return ResponseEntity.ok(ApiResponse.success("이미지가 업로드되었습니다."));
    }

    @Operation(summary = "게시글 이미지 순서만 변경", description = "게시글의 이미지 순서(orderIndex)만 수정합니다.")
    @PatchMapping("/{postId}/order")
    public ResponseEntity<ApiResponse<Void>> updateImageOrder(
            @PathVariable Long postId,
            @RequestBody PostImageOrderUpdateRequestDto requestDto
    ) {
        Long memberId = securityUtil.getCurrentMemberId();
        Post post = postImageService.findPostByIdAndValidateOwner(postId, memberId);
        postImageService.updateOrderIndexes(post, requestDto.getOrderIndexMap());
        return ResponseEntity.ok(ApiResponse.success("이미지 순서가 변경되었습니다."));
    }
}
