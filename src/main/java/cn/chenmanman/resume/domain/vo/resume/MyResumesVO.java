package cn.chenmanman.resume.domain.vo.resume;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyResumesVO {

    private Long id;
    private String title;
    private LocalDateTime updateTime;
    private Long templateId;
    private String previewImageUrl;
}
