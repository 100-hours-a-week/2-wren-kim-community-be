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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/posts/";

    // 게시글 작성
    @Transactional
    public PostCreateResponseDto createPost(PostCreateRequestDto requestDto, List<MultipartFile> images) {
        // ✅ 사용자 검증
        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // ✅ 게시글 저장
        Post post = postRepository.save(requestDto.toEntity(author));

        // ✅ 이미지 저장 (로컬 파일 시스템)
        List<PostImage> postImages = saveImagesLocally(images, post);
        postImageRepository.saveAll(postImages);

        return new PostCreateResponseDto(post, postImages);
    }

    private List<PostImage> saveImagesLocally(List<MultipartFile> images, Post post) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream().map(image -> {
            try {
                String savedFilePath = saveFile(image);
                return new PostImage(post, savedFilePath);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }).collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("Failed to create upload directory: " + UPLOAD_DIR);
        }

        String extension = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFileName = UUID.randomUUID().toString() + extension;
        String filePath = UPLOAD_DIR + File.separator + newFileName;

        File destination = new File(filePath);
        file.transferTo(destination);

        return filePath;
    }

    // 게시글 상세 조회
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
