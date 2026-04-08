package cn.chenmanman.resume.domain.vo.resume;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeVersionsVO {
    private Long versionId;
    private Long resumeId;
    private Integer versionNo;
    private String changeNote;
    private Object contentJson;
    private Object layoutJson;
    private LocalDateTime createTime;
}
