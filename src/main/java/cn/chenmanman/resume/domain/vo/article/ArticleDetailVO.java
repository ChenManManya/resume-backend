package cn.chenmanman.resume.domain.vo.article;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticleDetailVO {

    private Long articleId;
    private String coverUrl;
    private String title;
    private String content;
    private List<String> tags;
    private Integer viewNum;
    private Integer status;
    private LocalDateTime publishedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}
