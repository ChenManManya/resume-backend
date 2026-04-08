package cn.chenmanman.resume.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {

    private final LocalUploadProperties localUploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPath = normalizePublicPath(localUploadProperties.getPublicPath());
        Path uploadRoot = resolveUploadRoot();
        registry.addResourceHandler(publicPath + "/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }

    private Path resolveUploadRoot() {
        Path baseDirPath = Paths.get(localUploadProperties.getBaseDir());
        if (!baseDirPath.isAbsolute()) {
            baseDirPath = Paths.get(System.getProperty("user.dir")).resolve(baseDirPath);
        }
        return baseDirPath.normalize();
    }

    private String normalizePublicPath(String publicPath) {
        String normalized = publicPath == null || publicPath.isBlank() ? "/uploads" : publicPath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
