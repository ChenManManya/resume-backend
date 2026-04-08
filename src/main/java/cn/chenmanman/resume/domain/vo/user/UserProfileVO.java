package cn.chenmanman.resume.domain.vo.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phoneNumber;

    private Integer employmentStatus;

    private Integer status;
}
