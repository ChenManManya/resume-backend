package cn.chenmanman.resume.domain.entity.article;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("articles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesEntity implements Serializable {

    @TableId
    private Long id;

    private String coverUrl;

    private String title;

    private String content;

    private String tags;

    private Integer viewNum;

    private Integer status;

    private LocalDateTime publishedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
