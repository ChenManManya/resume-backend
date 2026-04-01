package cn.chenmanman.resume.service;

import cn.chenmanman.resume.domain.dto.user.UserLoginPostRequest;
import cn.chenmanman.resume.domain.dto.user.UserRegisterPostRequest;

public interface IAuthService {

    /**
     * 用户登录
     * @param request 用户登录请求体
     * */
    String login(UserLoginPostRequest request);

    /**
     * 用户注册
     * @param request 用户注册请求体
     * */
    void register(UserRegisterPostRequest request);
}
