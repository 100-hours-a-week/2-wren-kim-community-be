package ktb.community.be.domain.image.application;

import ktb.community.be.domain.image.dao.PostImageRepository;
import ktb.community.be.domain.image.domain.PostImage;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PostImageService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final FileStorageService fileStorageService;

    /**
     * 게시글 ID와 회원 ID를 기반으로 게시글을 조회하고 작성자인지 검증합니다.
     * (이미지 순서 변경이나 업로드 시 소유자 검증용)
     */
    @Transactional(readOnly = true)
    public Post findPostByIdAndValidateOwner(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!post.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return post;
    }

    /**
     * 게시글 이미지 중 유지하지 않을 이미지들을 Soft Delete 처리
     */
    @Transactional
    public void deleteImages(Post post, List<Long> keepImageIds) {
        List<PostImage> existingImages = postImageRepository.findAllByPostId(post.getId());
        List<PostImage> toDelete = existingImages.stream()
                .filter(img -> keepImageIds == null || !keepImageIds.contains(img.getId()))
                .toList();
        toDelete.forEach(PostImage::softDelete);
        postImageRepository.saveAll(toDelete);
    }

    /**
     * 게시글 이미지들 순서 수정
     */
    @Transactional
    public void updateOrderIndexes(Post post, Map<Long, Integer> orderIndexMap) {
        List<PostImage> existingImages = postImageRepository.findAllByPostId(post.getId());
        Map<Long, PostImage> imageMap = existingImages.stream()
                .collect(Collectors.toMap(PostImage::getId, img -> img));

        for (Map.Entry<Long, Integer> entry : orderIndexMap.entrySet()) {
            PostImage image = imageMap.get(entry.getKey());
            if (image != null) image.updateOrderIndex(entry.getValue());
        }
        postImageRepository.saveAll(existingImages);
    }

    /**
     * 새로 업로드된 게시글 이미지 저장
     */
    @Transactional
    public List<PostImage> saveNewImages(Post post, Member member, List<MultipartFile> images, List<Integer> orderIndexes) {
        if (images == null || images.isEmpty()) return List.of();
        return IntStream.range(0, images.size())
                .mapToObj(i -> PostImage.builder()
                        .post(post)
                        .member(member)
                        .imageUrl(fileStorageService.storeFile(images.get(i), "posts"))
                        .orderIndex(orderIndexes.get(i))
                        .isDeleted(false)
                        .build())
                .toList();
    }
}
