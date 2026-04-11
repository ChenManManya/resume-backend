package cn.chenmanman.resume.domain.entity.resume;

import cn.chenmanman.resume.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 简历主表
 * @TableName resumes
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="resumes")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResumesEntity extends BaseEntity implements Serializable {
    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 简历标题
     */
    private String title;

    /**
     * 当前模板ID
     */
    private Long templateId;

    /**
     * 状态：draft/published/archived
     */
    private String status;

    /**
     * 简历内容JSON字符串
     */
    private String contentJson;

    /**
     * 布局配置JSON字符串
     */
    private String layoutJson;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
