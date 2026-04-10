package cn.chenmanman.resume.domain.vo.resume;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserFavoriteVO {

    private Long templateId;
    private String name;
    private String previewImageUrl;
    private LocalDateTime createTime;
}
