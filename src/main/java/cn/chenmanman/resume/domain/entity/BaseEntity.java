package cn.chenmanman.resume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 通用实体
 * author: 陈慢慢
 * */
@SuperBuilder
@Data
public class BaseEntity {
    @TableId
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private Integer isDeleted;
}
