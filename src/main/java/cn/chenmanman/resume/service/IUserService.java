package cn.chenmanman.resume.service;

import cn.chenmanman.resume.domain.dto.user.UpdateUserProfileRequestPut;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.domain.vo.user.UserProfileVO;

import java.util.List;

public interface IUserService {

    UserProfileVO getProfile();

    UserProfileVO updateProfile(UpdateUserProfileRequestPut request);

    List<TemplatesVO> listRecommendTemplates(Integer limit);
}
