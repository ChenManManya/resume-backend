package cn.chenmanman.resume.domain.entity.user;

import cn.chenmanman.resume.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 用户表
 * @TableName sys_user
 * @author 陈慢慢
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="sys_user")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserEntity extends BaseEntity implements Serializable {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String passwordHash;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 当前身份(0:在职,1:应届,2:在校,3:无工作)
     */
    private Integer employmentStatus;


    /**
     * 用户状态(0:正常, 1:禁用)
     * */
    private Integer status;


    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}