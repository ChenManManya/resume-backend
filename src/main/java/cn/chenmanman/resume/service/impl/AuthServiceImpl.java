package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.enums.UserStatusEnum;
import cn.chenmanman.resume.common.error.UserErrorCode;
import cn.chenmanman.resume.domain.dto.user.UserLoginPostRequest;
import cn.chenmanman.resume.domain.dto.user.UserRegisterPostRequest;
import cn.chenmanman.resume.domain.entity.user.SysUserEntity;
import cn.chenmanman.resume.mapper.SysUserMapper;
import cn.chenmanman.resume.service.IAuthService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.chenmanman.resume.utils.RedisUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {


    private final SysUserMapper userMapper;

    private final RedisUtil redisUtil;

    private final PasswordEncoder passwordEncoder;


    /**
     * 用户登录
     *
     * @param request 用户登录请求体
     *
     */
    @Override
    public String login(UserLoginPostRequest request) {

        String redisKey = "manman_resume:login:captcha:" + request.getCaptchaKey();
        String cacheCode = redisUtil.get(redisKey);
        BizAssert.notNull(cacheCode, UserErrorCode.CAPTCHA_EXPIRE);
        BizAssert.isTrue(cacheCode.equalsIgnoreCase(request.getCaptchaCode()), UserErrorCode.CAPTCHA_WRONG);

        // 验证码校验成功, 删除验证码
        redisUtil.delete(redisKey);

        // 校验账号
        SysUserEntity userDb = userMapper.selectOne(Wrappers.lambdaQuery(SysUserEntity.class)
                .eq(SysUserEntity::getUsername, request.getUsername()));

        BizAssert.notNull(userDb, UserErrorCode.USERNAME_NOT_FOUND);



        BizAssert.notNull(userDb, UserErrorCode.USERNAME_NOT_FOUND);
        // 校验密码
        BizAssert.isTrue(
                passwordEncoder.matches(request.getPassword(), userDb.getPasswordHash()),
                UserErrorCode.PASSWORD_NOT_MATCHES
        );
        // 检查用户状态
        BizAssert.isTrue(UserStatusEnum.DISABLED.getValue().equals(userDb.getStatus()), UserErrorCode.USER_FORBIDDEN);
        StpUtil.login(userDb.getId());
        return StpUtil.getTokenValue();
    }

    /**
     * 用户注册
     *
     * @param request 用户注册请求体
     *
     */
    @Override
    public void register(UserRegisterPostRequest request) {
        String redisKey = "manman_resume:login:captcha:" + request.getCaptchaKey();
        String cacheCode = redisUtil.get(redisKey);
        BizAssert.notNull(cacheCode, UserErrorCode.CAPTCHA_EXPIRE);
        BizAssert.isTrue(cacheCode.equalsIgnoreCase(request.getCaptchaCode()), UserErrorCode.CAPTCHA_WRONG);

        // 验证码校验成功, 删除验证码
        redisUtil.delete(redisKey);
        // 检查用户是否存在
        SysUserEntity userDb = userMapper.selectOne(Wrappers.lambdaQuery(SysUserEntity.class)
                .eq(SysUserEntity::getUsername, request.getUsername()));
        BizAssert.isNull(userDb, UserErrorCode.USER_EXIST);

        // 校验邮箱
        SysUserEntity emailDb = userMapper.selectOne(Wrappers.lambdaQuery(SysUserEntity.class)
                .eq(SysUserEntity::getEmail, request.getEmail()));
        BizAssert.isNull(emailDb, UserErrorCode.EMAIL_BANDED);

        String encodePassword = passwordEncoder.encode(request.getPassword());

        userDb = SysUserEntity.builder()
                .passwordHash(encodePassword)
                .username(request.getUsername())
                .email(request.getEmail())
                .build();

        userMapper.insert(userDb);
    }
}
