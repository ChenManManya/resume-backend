package cn.chenmanman.resume.service;

import cn.chenmanman.resume.domain.dto.user.UpdateUserProfileRequestPut;
import cn.chenmanman.resume.domain.vo.user.UserProfileVO;

public interface IUserService {

    UserProfileVO getProfile();

    UserProfileVO updateProfile(UpdateUserProfileRequestPut request);
}
