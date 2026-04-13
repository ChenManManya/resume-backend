package cn.chenmanman.resume.domain.vo.article;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticlePageVO {

    private Long articleId;
    private String coverUrl;
    private String title;
    private List<String> tags;
    private Integer viewNum;
    private String content;
    private Integer status;
    private LocalDateTime publishedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
