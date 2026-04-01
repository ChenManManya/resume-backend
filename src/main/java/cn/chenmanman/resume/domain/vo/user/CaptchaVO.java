package cn.chenmanman.resume.domain.vo.user;

import lombok.Data;

@Data
public class CaptchaVO {
    private String captchaKey;
    private String captchaImage;

    public CaptchaVO(String captchaKey, String captchaImage) {
        this.captchaKey = captchaKey;
        this.captchaImage = captchaImage;
    }
}