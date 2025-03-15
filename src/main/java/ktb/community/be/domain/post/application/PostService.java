package ktb.community.be.domain.post.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.like.dao.PostLikeRepository;
import ktb.community.be.domain.like.domain.PostLike;
import ktb.community.be.domain.post.dao.PostImageRepository;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
import ktb.community.be.domain.post.dto.PostCreateRequestDto;
import ktb.community.be.domain.post.dto.PostCreateResponseDto;
import ktb.community.be.domain.post.dto.PostDetailResponseDto;
import ktb.community.be.domain.post.dto.PostUpdateRequestDto;
import ktb.community.be.domain.user.dao.UserRepository;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    /*
    게시글 작성
     */
    @Transactional
    public PostCreateResponseDto createPost(PostCreateRequestDto requestDto, List<MultipartFile> images, List<Integer> orderIndexes) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.save(requestDto.toEntity(user));

        // 사용자 지정 순서 반영
        List<PostImage> postImages = fileStorageService.storeImages(images, orderIndexes, post, user);
        postImageRepository.saveAll(postImages);

        return new PostCreateResponseDto(post, postImages);
    }

    /*
    게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public PostDetailResponseDto getPostDetail(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<PostComment> comments = postCommentRepository.findAllByPostId(postId);
        List<PostImage> images = postImageRepository.findAllByPostId(postId);
        int likeCount = postLikeRepository.countByPostId(postId); // 좋아요 수 직접 조회

        return new PostDetailResponseDto(post, likeCount, images, buildCommentHierarchy(comments));
    }

    private List<CommentResponseDto> buildCommentHierarchy(List<PostComment> comments) {
        if (comments.isEmpty()) return Collections.emptyList();

        Map<Long, CommentResponseDto> commentMap = new HashMap<>();
        List<CommentResponseDto> topLevelComments = new ArrayList<>();

        // 모든 댓글을 Map에 저장
        for (PostComment comment : comments) {
            commentMap.put(comment.getId(), new CommentResponseDto(comment));
        }

        // 부모-자식 관계 설정
        for (PostComment comment : comments) {
            Long parentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;

            if (parentId == null) {
                // 부모 댓글이 없는 경우 최상위 댓글로 처리
                topLevelComments.add(commentMap.get(comment.getId()));
            } else {
                // 부모 댓글 찾기
                CommentResponseDto parentComment = commentMap.get(parentId);

                if (parentComment == null) {
                    // 삭제된 부모 댓글을 처리하는 새로운 CommentResponseDto 생성 (Setter 없이)
                    parentComment = createDeletedCommentDto(parentId);
                    commentMap.put(parentId, parentComment);

                    // 부모 댓글이 없으면 최상위 댓글 리스트에 추가
                    topLevelComments.add(parentComment);
                }

                // 부모 댓글에 대댓글 추가
                parentComment.getReplies().add(commentMap.get(comment.getId()));
            }
        }

        return topLevelComments;
    }

    // 삭제된 부모 댓글을 처리하는 메서드 (Setter 없이 생성자로 처리)
    private CommentResponseDto createDeletedCommentDto(Long parentId) {
        return new CommentResponseDto(parentId, "삭제된 댓글입니다.");
    }

    /*
    게시글 수정
     */
    @Transactional
    public void updatePost(Long postId, String requestData, List<MultipartFile> newImages, String orderIndexesJson) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // JSON 데이터를 객체로 변환
        PostUpdateRequestDto updateRequest = parseUpdateRequestData(requestData);

        // 기존 이미지 조회
        List<PostImage> existingImages = postImageRepository.findAllByPostId(postId);
        Map<Long, PostImage> existingImageMap = existingImages.stream()
                .collect(Collectors.toMap(PostImage::getId, image -> image));

        // 유지할 이미지 ID 리스트
        List<Long> keepImageIds = updateRequest.getKeepImageIds();

        // Soft Delete할 이미지 리스트 추출
        List<PostImage> imagesToDelete = existingImages.stream()
                .filter(image -> keepImageIds == null || !keepImageIds.contains(image.getId()))
                .toList();

        // Soft Delete 적용 (한 번에 batch update 처리)
        if (!imagesToDelete.isEmpty()) {
            for (PostImage image : imagesToDelete) {
                image.softDelete();
            }
            postImageRepository.saveAll(imagesToDelete);
        }

        // 기존 이미지의 orderIndex 업데이트 최적화
        if (updateRequest.hasOrderIndexUpdate()) {
            Map<Long, Integer> orderIndexMap = updateRequest.getOrderIndexMap();
            for (Map.Entry<Long, Integer> entry : orderIndexMap.entrySet()) {
                PostImage image = existingImageMap.get(entry.getKey());
                if (image != null && !image.getOrderIndex().equals(entry.getValue())) {
                    image.updateOrderIndex(entry.getValue());
                }
            }
        }

        // 변경 사항 있는 기존 이미지만 업데이트
        postImageRepository.saveAll(existingImages);

        // 새 이미지 추가 (불필요한 INSERT 방지)
        if (newImages != null && !newImages.isEmpty()) {
            List<Integer> orderIndexes = parseOrderIndexes(orderIndexesJson, newImages);
            List<PostImage> newPostImages = fileStorageService.storeImages(newImages, orderIndexes, post, post.getUser());
            postImageRepository.saveAll(newPostImages);
        }

        // 제목과 내용 업데이트
        post.update(updateRequest.getTitle(), updateRequest.getContent());
    }

    private PostUpdateRequestDto parseUpdateRequestData(String requestData) {
        try {
            return new ObjectMapper().readValue(requestData, PostUpdateRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSON_FORMAT, "JSON 파싱 오류: " + e.getMessage());
        }
    }

    private List<Integer> parseOrderIndexes(String orderIndexesJson, List<MultipartFile> images) {
        try {
            if (orderIndexesJson == null || images == null || images.isEmpty()) return List.of();
            List<Integer> orderIndexes = new ObjectMapper().readValue(orderIndexesJson, List.class);
            if (orderIndexes.size() != images.size()) {
                throw new CustomException(ErrorCode.INVALID_REQUEST, "이미지 개수와 orderIndex 개수가 맞지 않습니다.");
            }
            return orderIndexes;
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSON_FORMAT, "JSON 파싱 오류: " + e.getMessage());
        }
    }

    /*
    게시글 삭제
    */
    @Transactional
    public void deletePost(Long postId) {
        // 삭제할 게시글 조회 (Soft Delete 적용되지 않은 게시글만)
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 연관된 데이터 조회
        List<PostComment> comments = postCommentRepository.findAllByPostId(postId);
        List<PostImage> images = postImageRepository.findAllByPostId(postId);
        List<PostLike> likes = postLikeRepository.findAllByPostId(postId);

        // 댓글 Soft Delete
        for (PostComment comment : comments) {
            comment.softDelete();
        }
        postCommentRepository.saveAll(comments); // Batch Update

        // 이미지 Soft Delete
        for (PostImage image : images) {
            image.softDelete();
        }
        postImageRepository.saveAll(images); // Batch Update

        // 좋아요 Soft Delete (있을 때만 처리)
        for (PostLike like : likes) {
            like.softDelete();
        }
        postLikeRepository.saveAll(likes); // Batch Update

        // 게시글 Soft Delete
        post.softDelete();
        postRepository.save(post); // 변경 감지 적용
    }
}
