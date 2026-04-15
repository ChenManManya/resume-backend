package cn.chenmanman.resume.domain.dto.resume;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RenameResumeRequest {

    private Long resumeId;

    @NotBlank
    private String newTitle;
}
