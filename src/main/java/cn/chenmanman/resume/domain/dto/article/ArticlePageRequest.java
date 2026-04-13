package cn.chenmanman.resume.domain.dto.article;

import cn.chenmanman.resume.common.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ArticlePageRequest extends PageRequest {

    private Integer status;

    private String tag;

    private String keyword;
}
