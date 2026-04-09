package cn.chenmanman.resume.utils;

import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.common.error.UserErrorCode;
import cn.chenmanman.resume.common.exception.BusinessException;
import cn.chenmanman.resume.config.LocalUploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalFileUploadUtil {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp");

    private final LocalUploadProperties localUploadProperties;
    private final ServerProperties serverProperties;

    public String uploadAvatar(MultipartFile file) {
        validateAvatarFile(file);
        return upload(file, localUploadProperties.getAvatarDir());
    }


    public String uploadPhoto(MultipartFile file) {
        validatePhotoFile(file);
        return upload(file, localUploadProperties.getResumePhotoDir());
    }

    public String upload(MultipartFile file, String subDirectory) {
        BizAssert.notNull(file, UserErrorCode.AVATAR_FILE_EMPTY);
        BizAssert.isFalse(file.isEmpty(), UserErrorCode.AVATAR_FILE_EMPTY);

        String targetSubDirectory = normalizeSubDirectory(subDirectory);
        Path uploadDirectory = resolveUploadRoot().resolve(targetSubDirectory).normalize();

        try {
            Files.createDirectories(uploadDirectory);
            String storedFileName = buildStoredFileName(file.getOriginalFilename());
            Path targetFile = uploadDirectory.resolve(storedFileName).normalize();
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return buildPublicUrl(targetSubDirectory, storedFileName);
        } catch (IOException e) {
            throw new BusinessException(UserErrorCode.AVATAR_UPLOAD_FAILED);
        }
    }

    public void deleteByPublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return;
        }

        String relativePath = stripPublicUrlPrefix(publicUrl);
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        Path targetFile = resolveUploadRoot().resolve(relativePath).normalize();
        if (!targetFile.startsWith(resolveUploadRoot())) {
            return;
        }

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException ignored) {
        }
    }

    private Path resolveUploadRoot() {
        Path baseDirPath = Paths.get(localUploadProperties.getBaseDir());
        if (!baseDirPath.isAbsolute()) {
            baseDirPath = Paths.get(System.getProperty("user.dir")).resolve(baseDirPath);
        }
        return baseDirPath.normalize();
    }

    private String buildStoredFileName(String originalFilename) {
        String extension = extractExtension(originalFilename);
        return UUID.randomUUID() + extension;
    }

    private String buildPublicUrl(String subDirectory, String fileName) {
        return normalizeContextPath() + normalizePublicPath() + "/" + subDirectory + "/" + fileName;
    }

    private String normalizeSubDirectory(String subDirectory) {
        if (subDirectory == null || subDirectory.isBlank()) {
            return "default";
        }
        String normalized = subDirectory.replace('\\', '/').trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isBlank() ? "default" : normalized;
    }

    private void validateAvatarFile(MultipartFile file) {
        BizAssert.notNull(file, UserErrorCode.AVATAR_FILE_EMPTY);
        BizAssert.isFalse(file.isEmpty(), UserErrorCode.AVATAR_FILE_EMPTY);
        BizAssert.isTrue(file.getSize() <= localUploadProperties.getMaxAvatarSizeBytes(), UserErrorCode.AVATAR_FILE_TOO_LARGE);

        String extension = extractExtension(file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        BizAssert.isTrue(ALLOWED_IMAGE_EXTENSIONS.contains(extension), UserErrorCode.AVATAR_FILE_TYPE_INVALID);
    }

    private void validatePhotoFile(MultipartFile file) {
        BizAssert.notNull(file, ResumesErrorCode.PHOTO_FILE_EMPTY);

        BizAssert.isFalse(file.isEmpty(), ResumesErrorCode.PHOTO_FILE_EMPTY);
        BizAssert.isTrue(file.getSize() <= localUploadProperties.getMaxPhotoSizeBytes(), ResumesErrorCode.PHOTO_FILE_TOO_LARGE);

        String extension = extractExtension(file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        BizAssert.isTrue(ALLOWED_IMAGE_EXTENSIONS.contains(extension), ResumesErrorCode.PHOTO_FILE_TYPE_INVALID);

    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String normalizePublicPath() {
        String publicPath = localUploadProperties.getPublicPath();
        String normalizedPublicPath = publicPath == null || publicPath.isBlank() ? "/uploads" : publicPath.trim();
        if (!normalizedPublicPath.startsWith("/")) {
            normalizedPublicPath = "/" + normalizedPublicPath;
        }
        if (normalizedPublicPath.endsWith("/")) {
            normalizedPublicPath = normalizedPublicPath.substring(0, normalizedPublicPath.length() - 1);
        }
        return normalizedPublicPath;
    }

    private String normalizeContextPath() {
        if (serverProperties.getServlet() == null) {
            return "";
        }
        String contextPath = serverProperties.getServlet().getContextPath();
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath.trim())) {
            return "";
        }
        String normalized = contextPath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String stripPublicUrlPrefix(String publicUrl) {
        String normalizedPublicPath = normalizePublicPath();
        String normalizedContextPath = normalizeContextPath();
        String fullPrefix = normalizedContextPath + normalizedPublicPath + "/";
        String fallbackPrefix = normalizedPublicPath + "/";

        if (publicUrl.startsWith(fullPrefix)) {
            return publicUrl.substring(fullPrefix.length());
        }
        if (publicUrl.startsWith(fallbackPrefix)) {
            return publicUrl.substring(fallbackPrefix.length());
        }
        return null;
    }
}
