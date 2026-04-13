package cn.chenmanman.resume.domain.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateArticleRequestPut {

    private String coverUrl;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private List<String> tags;

    @NotNull
    private Integer status;
}
