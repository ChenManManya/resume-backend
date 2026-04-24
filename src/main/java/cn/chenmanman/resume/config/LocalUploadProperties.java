package cn.chenmanman.resume.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.upload")
public class LocalUploadProperties {

    private String baseDir = "uploads";

    private String publicPath = "/uploads";

    private String avatarDir = "avatars";

    private String resumePhotoDir = "resume_photos";

    private String articleCoverDir = "article_covers";
    private String templatesCoverDir = "templates_cover";

    private long maxAvatarSizeBytes = 2 * 1024 * 1024;
    private long maxPhotoSizeBytes = 2 * 1024 * 1024;
    private long maxArticleCoverSizeBytes = 5 * 1024 * 1024;
}
