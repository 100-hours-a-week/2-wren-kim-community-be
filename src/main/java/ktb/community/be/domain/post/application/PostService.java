package ktb.community.be.domain.post.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.like.dao.PostLikeRepository;
import ktb.community.be.domain.like.domain.PostLike;
import ktb.community.be.domain.post.dao.PostImageRepository;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
import ktb.community.be.domain.post.dto.*;
import ktb.community.be.domain.member.dao.MemberRepository;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.util.CommentHierarchyBuilder;
import ktb.community.be.global.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;
    private final ViewCountService viewCountService;

    /**
     * 게시글 작성
     */
    @Transactional
    public PostCreateResponseDto createPost(PostCreateRequestDto requestDto, List<MultipartFile> images, List<Integer> orderIndexes) {
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.save(requestDto.toEntity(member));

        // 사용자 지정 순서 반영
        List<PostImage> postImages = fileStorageService.storePostImages(images, orderIndexes, post, member);
        postImageRepository.saveAll(postImages);

        return PostCreateResponseDto.from(post, postImages);
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public PostDetailResponseDto getPostDetail(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 비동기로 조회수 증가 실행
        viewCountService.incrementViewCount(postId);

        List<PostComment> comments = postCommentRepository.findAllByPostId(postId);
        List<PostImage> images = postImageRepository.findAllByPostId(postId);
        int likeCount = postLikeRepository.countByPostId(postId);

        return PostDetailResponseDto.from(post, likeCount, images, CommentHierarchyBuilder.buildCommentHierarchy(comments));
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostDetailResponseDto updatePost(Long postId, String requestData, List<MultipartFile> newImages, String orderIndexesJson) {
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
            List<PostImage> newPostImages = fileStorageService.storePostImages(newImages, orderIndexes, post, post.getMember());
            postImageRepository.saveAll(newPostImages);
        }

        // 제목과 내용 업데이트
        post.update(updateRequest.getTitle(), updateRequest.getContent());

        return PostDetailResponseDto.from(post, postLikeRepository.countByPostId(postId), postImageRepository.findAllByPostId(postId), List.of());
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

    /**
     * 게시글 삭제
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
            like.softDeleteByPostDeletion();
        }
        postLikeRepository.saveAll(likes); // Batch Update

        // 게시글 Soft Delete
        post.softDelete();
        postRepository.save(post); // 변경 감지 적용
    }

    /**
     * 게시글 전체 조회 (커서 기반 페이지네이션)
     */
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getAllPosts(LocalDateTime cursor, Pageable pageable) {
        List<Post> posts = postRepository.findByCursor(cursor, pageable);
        return posts.stream().map(PostListResponseDto::from).collect(Collectors.toList());
    }
}
