package ktb.community.be.domain.post.application;

import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.like.dao.PostLikeRepository;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.dto.PostDetailResponseDto;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional(readOnly = true)
    public PostDetailResponseDto getPostDetail(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<PostComment> comments = postCommentRepository.findAllByPostId(postId);

        return new PostDetailResponseDto(post, buildCommentHierarchy(comments));
    }

    private List<CommentResponseDto> buildCommentHierarchy(List<PostComment> comments) {
        if (comments.isEmpty()) return Collections.emptyList();

        Map<Long, CommentResponseDto> commentMap = comments.stream()
                .collect(Collectors.toMap(PostComment::getId, CommentResponseDto::new));

        List<CommentResponseDto> rootComments = new ArrayList<>();

        comments.forEach(comment -> {
            CommentResponseDto dto = commentMap.get(comment.getId());
            if (comment.getParentComment() == null) {
                rootComments.add(dto);
            } else {
                commentMap.computeIfPresent(comment.getParentComment().getId(),
                        (key, parent) -> {
                            parent.getReplies().add(dto);
                            return parent;
                        });
            }
        });

        return rootComments;
    }
}
