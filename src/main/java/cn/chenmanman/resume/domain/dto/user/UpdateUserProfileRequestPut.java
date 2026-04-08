package cn.chenmanman.resume.domain.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserProfileRequestPut {

    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phoneNumber;

    private Integer employmentStatus;

    private MultipartFile avatarFile;
}
