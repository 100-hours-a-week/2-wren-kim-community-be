package ktb.community.be.domain.like.api;

import io.swagger.v3.oas.annotations.Operation;
import ktb.community.be.domain.like.application.PostLikeService;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.response.ApiResponseConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 좋아요 추가/취소", description = "사용자가 게시글에 좋아요를 누르면 추가되고, 다시 누르면 취소됩니다.")
    @ApiResponseConstants.CommonResponses
    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(
            @PathVariable Long postId,
            @RequestParam Long userId) {

        boolean isLiked = postLikeService.toggleLike(postId, userId);
        String message = isLiked ? "게시글에 좋아요를 눌렀습니다." : "게시글 좋아요를 취소했습니다.";
        return ResponseEntity.ok(ApiResponse.success(message, isLiked));
    }

    @Operation(summary = "게시글 좋아요 개수 조회", description = "해당 게시글의 좋아요 개수를 반환합니다.")
    @ApiResponseConstants.CommonResponses
    @GetMapping("/{postId}/count")
    public ResponseEntity<ApiResponse<Integer>> getLikeCount(@PathVariable Long postId) {
        int likeCount = postLikeService.getLikeCount(postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 좋아요 개수를 조회했습니다.", likeCount));
    }
}
