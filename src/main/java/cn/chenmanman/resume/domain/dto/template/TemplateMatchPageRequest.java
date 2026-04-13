package cn.chenmanman.resume.domain.dto.template;

import cn.chenmanman.resume.common.PageRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TemplateMatchPageRequest extends PageRequest {

    private String tag;

    private String category;

    private String keyword;
}
