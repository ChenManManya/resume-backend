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
 * 简历版本快照表
 * @TableName resume_versions
 */
@TableName(value ="resume_versions")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeVersionsEntity extends BaseEntity implements Serializable {
    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 快照标题
     */
    private String title;

    /**
     * 快照模板ID
     */
    private Long templateId;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 简历内容JSON
     */
    private Object contentJson;

    /**
     * 布局配置JSON
     */
    private Object layoutJson;

    /**
     * 渲染后的HTML快照
     */
    private String snapshotHtml;

    /**
     * 版本说明
     */
    private String changeNote;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
