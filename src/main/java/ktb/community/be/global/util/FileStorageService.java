package ktb.community.be.global.util;

import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
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

    private static final String PROFILE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile/";
    private static final String POST_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/posts/";

    // 프로필 이미지 저장 (1개만)
    public String storeProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "프로필 이미지는 필수입니다.");
        }

        try {
            ensureUploadDirExists(PROFILE_UPLOAD_DIR);
            String savedFilePath = generateUniqueFilePath(file, PROFILE_UPLOAD_DIR);
            file.transferTo(new File(savedFilePath));
            return savedFilePath;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED, "파일 저장 실패: " + file.getOriginalFilename());
        }
    }

    // 게시글 이미지 저장 (여러 개 가능)
    public List<PostImage> storePostImages(List<MultipartFile> images, List<Integer> orderIndexes, Post post, Member member) {
        if (images == null || images.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "게시글 이미지는 최소 1개 이상 필요합니다.");
        }

        if (orderIndexes == null || orderIndexes.size() != images.size()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미지 개수와 orderIndex 개수가 맞지 않습니다.");
        }

        return IntStream.range(0, images.size())
                .mapToObj(i -> savePostImage(images.get(i), post, member, orderIndexes.get(i)))
                .collect(Collectors.toList());
    }

    // 게시글 이미지 개별 저장
    private PostImage savePostImage(MultipartFile file, Post post, Member member, int orderIndex) {
        try {
            ensureUploadDirExists(POST_UPLOAD_DIR);
            String savedFilePath = generateUniqueFilePath(file, POST_UPLOAD_DIR);
            file.transferTo(new File(savedFilePath));

            return PostImage.builder()
                    .post(post)
                    .member(member)
                    .imageUrl(savedFilePath)
                    .orderIndex(orderIndex)
                    .isDeleted(false)
                    .build();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED, "파일 저장 실패: " + file.getOriginalFilename());
        }
    }

    // 폴더가 없으면 생성
    private void ensureUploadDirExists(String uploadDir) throws IOException {
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            boolean isCreated = uploadDirFile.mkdirs();
            System.out.println("📂 디렉토리 생성됨: " + uploadDirFile.getAbsolutePath() + " -> " + isCreated);
            if (!isCreated) {
                throw new IOException("업로드 디렉터리 생성 실패: " + uploadDir);
            }
        }
    }

    // 저장할 파일명 생성 (UUID 사용)
    private String generateUniqueFilePath(MultipartFile file, String uploadDir) {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        return uploadDir + UUID.randomUUID() + extension;
    }
}
