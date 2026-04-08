package cn.chenmanman.resume.domain.dto.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateResumeVersionsRequestPost {
    private String changeNote;

    @NotBlank
    private String title;

    @NotNull
    private Long templateId;

    @NotNull
    private Object contentJson;

    @NotNull
    private Object layoutJson;
}
