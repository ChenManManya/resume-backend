package cn.chenmanman.resume.domain.vo.resume;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyResumesVO {

    private Long id;
    private String title;
    private LocalDateTime updateTime;
    private Long templateId;
    private String status;
}
