package cn.chenmanman.resume.domain.dto.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateResumesRequestPost {
    @NotNull
    private Long templateId;

    @NotBlank
    private String title;
}
