package ktb.community.be.domain.post.application;

import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.like.dao.PostLikeRepository;
import ktb.community.be.domain.post.dao.PostImageRepository;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
import ktb.community.be.domain.post.dto.PostCreateRequestDto;
import ktb.community.be.domain.post.dto.PostCreateResponseDto;
import ktb.community.be.domain.post.dto.PostDetailResponseDto;
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

    // 게시글 작성
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

    // 게시글 상세 조회
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
}
