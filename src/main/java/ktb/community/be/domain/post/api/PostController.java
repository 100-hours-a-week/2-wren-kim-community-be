package ktb.community.be.domain.post.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import ktb.community.be.domain.post.application.PostService;
import ktb.community.be.domain.post.dto.PostCreateRequestDto;
import ktb.community.be.domain.post.dto.PostCreateResponseDto;
import ktb.community.be.domain.post.dto.PostDetailResponseDto;
import ktb.community.be.domain.post.dto.PostListResponseDto;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.response.ApiResponseConstants;
import ktb.community.be.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "게시글 작성", description = "게시글을 작성합니다.")
    @ApiResponseConstants.CommonResponses
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponseDto>> createPost(
            @RequestPart("data") String requestData,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "orderIndexes", required = false) String orderIndexesJson) {

        PostCreateRequestDto requestDto = parseRequestData(requestData);
        Long memberId = securityUtil.getCurrentMemberId();

        PostCreateResponseDto responseDto = postService.createPost(memberId, requestDto, images, orderIndexesJson);
        return ResponseEntity.ok(ApiResponse.success("게시글이 작성되었습니다.", responseDto));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID를 기반으로 상세 정보를 조회합니다.")
    @ApiResponseConstants.PostDetailResponses
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("게시글을 조회했습니다.", postService.getPostDetail(postId)));
    }

    private PostCreateRequestDto parseRequestData(String requestData) {
        try {
            return objectMapper.readValue(requestData, PostCreateRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSON_FORMAT, "JSON 파싱 오류: " + e.getMessage());
        }
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> updatePost(
            @PathVariable Long postId,
            @RequestPart("data") String requestData,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "orderIndexes", required = false) String orderIndexesJson) {
        Long memberId = securityUtil.getCurrentMemberId();
        PostDetailResponseDto updatedPost = postService.updatePost(postId, memberId, requestData, images, orderIndexesJson);
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", updatedPost));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (Soft Delete)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        Long memberId = securityUtil.getCurrentMemberId();
        postService.deletePost(postId, memberId);
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다."));
    }

    @Operation(summary = "게시글 전체 조회", description = "커서 기반 페이지네이션을 사용하여 게시글을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostListResponseDto>>> getAllPosts(
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "10") int size) {
        List<PostListResponseDto> posts = postService.getAllPosts(cursor, PageRequest.of(0, size));
        return ResponseEntity.ok(ApiResponse.success("게시글 목록을 조회했습니다.", posts));
    }
}
