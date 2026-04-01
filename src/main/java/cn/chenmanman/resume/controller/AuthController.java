package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.domain.dto.user.UserLoginPostRequest;
import cn.chenmanman.resume.domain.dto.user.UserRegisterPostRequest;
import cn.chenmanman.resume.service.IAuthService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid UserLoginPostRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid UserRegisterPostRequest request) {
        authService.register(request);
        return Result.success();
    }

    @GetMapping("/logout")
    public Result<String> logout() {
        log.debug("当前登录用户:{}", StpUtil.getLoginId());
        StpUtil.logout();
        return Result.success();
    }
}
