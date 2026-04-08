package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.domain.dto.user.UpdateUserProfileRequestPut;
import cn.chenmanman.resume.domain.vo.user.UserProfileVO;
import cn.chenmanman.resume.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;

    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        return Result.success(userService.getProfile());
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UserProfileVO> updateProfile(@Valid @ModelAttribute UpdateUserProfileRequestPut request) {
        return Result.success(userService.updateProfile(request));
    }
}
