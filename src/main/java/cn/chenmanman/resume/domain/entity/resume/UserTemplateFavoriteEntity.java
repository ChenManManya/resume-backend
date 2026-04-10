package cn.chenmanman.resume.domain.entity.resume;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("user_template_favorite")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTemplateFavoriteEntity {
    private Long userId;
    private Long templateId;
    @TableId
    private Long id;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
