package ktb.community.be.domain.post.application;

import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
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
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // 게시글 작성
    @Transactional
    public PostCreateResponseDto createPost(PostCreateRequestDto requestDto, List<MultipartFile> images) {
        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.save(requestDto.toEntity(author));
        List<PostImage> postImages = fileStorageService.storeImages(images, post);
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

        return new PostDetailResponseDto(post, images, buildCommentHierarchy(comments));
    }

    private List<CommentResponseDto> buildCommentHierarchy(List<PostComment> comments) {
        if (comments.isEmpty()) return Collections.emptyList();

        Map<Long, CommentResponseDto> commentMap = comments.stream()
                .collect(Collectors.toMap(PostComment::getId, CommentResponseDto::new));

        // 부모-자식 관계 매핑
        comments.forEach(comment -> {
            Long parentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
            if (parentId != null) {
                commentMap.get(parentId).getReplies().add(commentMap.get(comment.getId()));
            }
        });

        // 최상위 댓글만 필터링하여 반환
        return commentMap.values().stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .collect(Collectors.toList());
    }
}
