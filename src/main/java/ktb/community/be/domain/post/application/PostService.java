package ktb.community.be.domain.post.application;

import ktb.community.be.domain.comment.application.PostCommentService;
import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.image.application.PostImageService;
import ktb.community.be.domain.image.dao.PostImageRepository;
import ktb.community.be.domain.image.domain.PostImage;
import ktb.community.be.domain.like.dao.PostLikeRepository;
import ktb.community.be.domain.like.domain.PostLike;
import ktb.community.be.domain.member.dao.MemberRepository;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.dto.*;
import ktb.community.be.global.domain.BaseTimeEntity;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostImageRepository postImageRepository;
    private final MemberRepository memberRepository;
    private final PostImageService postImageService;
    private final PostCommentService postCommentService;
    private final FileStorageService fileStorageService;

    /**
     * 게시글에 대한 이미지 업로드 처리
     */
    @Transactional
    public void uploadImages(Long postId, Long memberId,
                             List<MultipartFile> images, List<Integer> orderIndexes) {
        Post post = findPostByIdAndValidateOwner(postId, memberId);
        List<PostImage> postImages = buildPostImages(images, orderIndexes, post, post.getMember());
        postImageRepository.saveAll(postImages);
    }

    /**
     * 게시글 생성 및 이미지 저장
     */
    @Transactional
    public PostCreateResponseDto createPost(Long memberId, PostCreateRequestDto requestDto,
                                            List<MultipartFile> images, List<Integer> orderIndexes) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.save(requestDto.toEntity(member));

        List<PostImage> postImages = List.of();
        if (images != null && !images.isEmpty()) {
            postImages = buildPostImages(images, orderIndexes, post, member);
            postImageRepository.saveAll(postImages);
        }

        return PostCreateResponseDto.from(post, postImages);
    }

    /**
     * 게시글 수정 및 이미지 변경 처리
     */
    @Transactional
    public PostDetailResponseDto updatePost(Long postId, Long memberId, PostUpdateWithImageRequestDto updateDto) {
        Post post = findPostByIdAndValidateOwner(postId, memberId);
        PostUpdateRequestDto data = updateDto.getPostData();

        postImageService.deleteImages(post, data.getKeepImageIds());
        if (data.hasOrderIndexUpdate()) {
            postImageService.updateOrderIndexes(post, data.getOrderIndexMap());
        }

        if (updateDto.getNewImages() != null && !updateDto.getNewImages().isEmpty()) {
            List<PostImage> newPostImages = postImageService.saveNewImages(post, post.getMember(), updateDto.getNewImages(), updateDto.getOrderIndexes());
            // 저장은 서비스 내부에서 진행됨
        }

        post.update(data.getTitle(), data.getContent());

        return PostDetailResponseDto.from(
                post,
                postLikeRepository.countByPostId(postId),
                postImageRepository.findAllByPostId(postId),
                List.of()
        );
    }

    /**
     * 게시글 상세 조회 (조회수 증가 포함)
     */
    @Transactional
    public PostDetailResponseDto getPostDetail(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.increaseViewCount();

        List<CommentResponseDto> comments = postCommentService.getCommentsByPostId(postId);
        List<PostImage> images = postImageRepository.findAllByPostId(postId);
        int likeCount = postLikeRepository.countByPostId(postId);

        return PostDetailResponseDto.from(post, likeCount, images, comments);
    }

    /**
     * 게시글 삭제 (댓글, 이미지, 좋아요도 함께 soft delete)
     */
    @Transactional
    public void deletePost(Long postId, Long memberId) {
        Post post = findPostByIdAndValidateOwner(postId, memberId);

        softDeleteAll(postCommentRepository.findAllByPostId(postId));
        softDeleteAll(postImageRepository.findAllByPostId(postId));
        softDeleteAll(postLikeRepository.findAllByPostId(postId));

        post.softDelete();
        postRepository.save(post);
    }

    /**
     * 전체 게시글 조회 (커서 기반 페이지네이션)
     */
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getAllPosts(LocalDateTime cursor, Pageable pageable) {
        return postRepository.findByCursor(cursor, pageable).stream()
                .map(post -> PostListResponseDto.from(post, postLikeRepository.countByPostId(post.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 게시글 ID와 회원 ID로 게시글 조회 및 작성자 검증
     */
    private Post findPostByIdAndValidateOwner(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!post.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return post;
    }

    /**
     * 게시글 이미지 목록 생성 (파일 저장 포함)
     */
    private List<PostImage> buildPostImages(List<MultipartFile> images, List<Integer> orderIndexes,
                                            Post post, Member member) {
        if (images == null || images.isEmpty()) return List.of();

        if (orderIndexes == null || orderIndexes.size() != images.size()) {
            throw new CustomException(ErrorCode.IMAGE_ORDER_INDEX_MISMATCH);
        }

        return IntStream.range(0, images.size())
                .mapToObj(i -> {
                    String imageUrl = fileStorageService.storeFile(images.get(i), "posts");
                    return PostImage.builder()
                            .post(post)
                            .member(member)
                            .imageUrl(imageUrl)
                            .orderIndex(orderIndexes.get(i))
                            .isDeleted(false)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 댓글, 이미지, 좋아요 등 다양한 엔티티 일괄 soft delete
     */
    private <T extends BaseTimeEntity> void softDeleteAll(List<T> entities) {
        entities.forEach(entity -> {
            if (entity instanceof PostImage image) image.softDelete();
            else if (entity instanceof PostComment comment) comment.softDelete();
            else if (entity instanceof PostLike like) like.softDeleteByPostDeletion();
        });
    }
}
