package cn.chenmanman.resume.domain.vo.resume;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumesVO {
    private Long resumeId;
    private String title;
    private Long templateId;
    private String status;
    private Object contentJson;
    private Object layoutJson;
}
