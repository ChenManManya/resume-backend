package cn.chenmanman.resume.domain.dto.template;

import cn.chenmanman.resume.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuestTemplateRecommendRequest extends PageRequest {

    private String category;

    private String tag;

    private String keyword;

    private Integer employmentStatus;
}
