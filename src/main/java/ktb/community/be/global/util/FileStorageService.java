package ktb.community.be.global.util;

import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.image.domain.PostImage;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FileStorageService {

    private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String PROFILE_SUB_DIR = "profile/";
    private static final String POST_SUB_DIR = "posts/";

    /**
     * 프로필 이미지 저장
     */
    public String storeProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "프로필 이미지는 필수입니다.");
        }
        return storeFile(file, PROFILE_SUB_DIR);
    }

    /**
     * 게시글 이미지 여러 개 저장
     */
    public List<PostImage> storePostImages(List<MultipartFile> images, List<Integer> orderIndexes, Post post, Member member) {
        if (images == null || images.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "게시글 이미지는 최소 1개 이상 필요합니다.");
        }

        if (orderIndexes == null || orderIndexes.size() != images.size()) {
            throw new CustomException(ErrorCode.IMAGE_ORDER_INDEX_MISMATCH);
        }

        return IntStream.range(0, images.size())
                .mapToObj(i -> savePostImage(images.get(i), post, member, orderIndexes.get(i)))
                .collect(Collectors.toList());
    }

    /**
     * 게시글 이미지 한 장 저장
     */
    private PostImage savePostImage(MultipartFile file, Post post, Member member, int orderIndex) {
        String savedFilePath = storeFile(file, POST_SUB_DIR);
        return PostImage.builder()
                .post(post)
                .member(member)
                .imageUrl(savedFilePath)
                .orderIndex(orderIndex)
                .isDeleted(false)
                .build();
    }

    /**
     * 범용 파일 저장 메서드
     */
    public String storeFile(MultipartFile file, String subDir) {
        try {
            String uploadDir = BASE_UPLOAD_DIR + subDir;
            ensureUploadDirExists(uploadDir);

            String fileName = generateUniqueFileName(file);
            String fullPath = uploadDir + fileName;

            file.transferTo(new File(fullPath));

            // DB에는 상대경로 (/uploads/posts/filename.png) 형식으로 반환
            return "/uploads/" + subDir + fileName;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED, "파일 저장 실패: " + file.getOriginalFilename());
        }
    }

    /**
     * 저장 디렉토리 존재 확인 및 생성
     */
    private void ensureUploadDirExists(String uploadDir) throws IOException {
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            boolean isCreated = uploadDirFile.mkdirs();
            System.out.println("디렉토리 생성됨: " + uploadDirFile.getAbsolutePath() + " -> " + isCreated);
            if (!isCreated) {
                throw new IOException("업로드 디렉터리 생성 실패: " + uploadDir);
            }
        }
    }

    /**
     * UUID 기반 고유 파일 경로 생성
     */
    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        return UUID.randomUUID() + extension;
    }
}
