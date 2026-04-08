package cn.chenmanman.resume.domain.vo.resume;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplatesVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String previewImageUrl;
    private String category;
    private Object schemaJson;
    private Object styleJson;
}
