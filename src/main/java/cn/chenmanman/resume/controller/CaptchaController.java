package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.domain.vo.user.CaptchaVO;
import cn.chenmanman.resume.utils.RedisUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

@RestController
public class CaptchaController {

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 生成验证码并且响应
     * */
    @GetMapping("/captcha")
    public CaptchaVO getCaptcha() {
        CircleCaptcha captcha = new CircleCaptcha(120, 40, 4, 20);
        String code = captcha.getCode();
        String captchaKey = IdUtil.simpleUUID();

        redisUtil.set("login:captcha:" + captchaKey, code, 5, TimeUnit.MINUTES);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        captcha.write(outputStream);
        String base64 = Base64.encode(outputStream.toByteArray());

        return new CaptchaVO(captchaKey, "data:image/png;base64," + base64);
    }
}