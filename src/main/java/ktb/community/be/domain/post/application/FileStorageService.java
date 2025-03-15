package ktb.community.be.domain.post.application;

import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.post.domain.PostImage;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/posts/";

    public List<PostImage> storeImages(List<MultipartFile> images, Post post) {
        if (images == null || images.isEmpty()) return List.of();

        return images.stream()
                .map(image -> saveFile(image, post))
                .collect(Collectors.toList());
    }

    private PostImage saveFile(MultipartFile file, Post post) {
        try {
            ensureUploadDirExists();
            String savedFilePath = generateUniqueFilePath(file);
            file.transferTo(new File(savedFilePath));
            return new PostImage(post, savedFilePath);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED, "파일 저장 실패: " + file.getOriginalFilename());
        }
    }

    private void ensureUploadDirExists() throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("업로드 디렉터리 생성 실패: " + UPLOAD_DIR);
        }
    }

    private String generateUniqueFilePath(MultipartFile file) {
        String extension = file.getOriginalFilename().contains(".") ?
                file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : "";
        return UPLOAD_DIR + UUID.randomUUID() + extension;
    }
}
