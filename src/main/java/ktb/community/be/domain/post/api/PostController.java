package ktb.community.be.domain.post.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import ktb.community.be.domain.post.application.PostService;
import ktb.community.be.domain.post.dto.PostCreateRequestDto;
import ktb.community.be.domain.post.dto.PostCreateResponseDto;
import ktb.community.be.domain.post.dto.PostDetailResponseDto;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.response.ApiResponseConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "게시글 작성", description = "게시글을 작성합니다.")
    @ApiResponseConstants.CommonResponses
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponseDto>> createPost(
            @RequestPart("data") String requestData,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        PostCreateRequestDto requestDto = parseRequestData(requestData);
        PostCreateResponseDto responseDto = postService.createPost(requestDto, images);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID를 기반으로 상세 정보를 조회합니다.")
    @ApiResponseConstants.PostDetailResponses
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostDetail(postId)));
    }

    private PostCreateRequestDto parseRequestData(String requestData) {
        try {
            return objectMapper.readValue(requestData, PostCreateRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSON_FORMAT, "JSON 파싱 오류: " + e.getMessage());
        }
    }
}
