package cn.chenmanman.resume.common.error;

import lombok.Data;
import lombok.Getter;

@Getter
public enum UserErrorCode implements ErrorCode {
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    CAPTCHA_EXPIRE(405, "验证码已过期"),
    CAPTCHA_WRONG(406, "验证码错误"),
    USERNAME_NOT_FOUND(407, "用户不存在"),
    PASSWORD_NOT_MATCHES(408, "密码不正确"),
    USER_FORBIDDEN(409, "用户已被禁用"),
    USER_EXIST(410, "用户已存在"),
    ;

    private final Integer code;
    private final String message;

    UserErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
