package cn.chenmanman.resume.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterPostRequest {
    @NotBlank(message = "用户名不能为空!")
    private String username;

    @NotBlank(message = "密码不能为空!")
    private String password;
    @NotBlank(message = "验证码不能为空!")
    private String captchaCode;
    private String captchaKey;
    private String email;
}
