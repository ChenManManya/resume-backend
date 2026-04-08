package cn.chenmanman.resume.domain.entity.resume;

import cn.chenmanman.resume.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 简历模板表
 * @TableName templates
 */
@TableName(value ="templates")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatesEntity extends BaseEntity implements Serializable {
    /**
     * 模板编码，如 minimal / online
     */
    private String code;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板预览图
     */
    private String previewImageUrl;

    /**
     * 模板分类
     */
    private String category;

    /**
     * 是否启用
     */
    private Integer isActive;

    /**
     * 模板结构配置
     */
    private Object schemaJson;

    /**
     * 模板样式配置
     */
    private Object styleJson;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}