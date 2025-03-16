package ktb.community.be.domain.comment.application;

import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentRequestDto;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.user.dao.UserRepository;
import ktb.community.be.domain.user.domain.User;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentResponseDto createComment(Long postId, CommentRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        PostComment comment = new PostComment(post, user, requestDto.getContent());
        postCommentRepository.save(comment);

        return new CommentResponseDto(comment);
    }

    /**
     * 대댓글 작성
     */
    @Transactional
    public CommentResponseDto createReply(Long postId, Long parentCommentId, CommentRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        PostComment parentComment = postCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "부모 댓글을 찾을 수 없습니다."));

        PostComment reply = new PostComment(post, user, requestDto.getContent(), parentComment);
        postCommentRepository.save(reply);

        return new CommentResponseDto(reply);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "댓글을 찾을 수 없습니다."));

        comment.updateContent(requestDto.getContent());
        postCommentRepository.save(comment);

        return new CommentResponseDto(comment);
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteComment(Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "댓글을 찾을 수 없습니다."));

        comment.softDelete();
        postCommentRepository.save(comment);
    }

    /**
     * 게시글에 달린 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return postCommentRepository.findAllByPostId(postId)
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}
