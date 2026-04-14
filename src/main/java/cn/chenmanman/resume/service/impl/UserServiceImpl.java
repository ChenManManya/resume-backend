package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.error.UserErrorCode;
import cn.chenmanman.resume.domain.dto.user.UpdateUserProfileRequestPut;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.domain.entity.user.SysUserEntity;
import cn.chenmanman.resume.domain.vo.user.UserProfileVO;
import cn.chenmanman.resume.mapper.SysUserMapper;
import cn.chenmanman.resume.service.IUserService;
import cn.chenmanman.resume.service.ITemplatesService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.chenmanman.resume.utils.LocalFileUploadUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {

    private final SysUserMapper sysUserMapper;
    private final LocalFileUploadUtil localFileUploadUtil;
    private final ITemplatesService templatesService;

    @Override
    public UserProfileVO getProfile() {
        return buildUserProfileVO(requireCurrentUser());
    }

    @Transactional
    @Override
    public UserProfileVO updateProfile(UpdateUserProfileRequestPut request) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserEntity currentUser = requireCurrentUser();

        SysUserEntity userUpdate = new SysUserEntity();
        userUpdate.setId(currentUser.getId());
        userUpdate.setNickname(request.getNickname());
        userUpdate.setEmail(request.getEmail());
        userUpdate.setPhoneNumber(request.getPhoneNumber());
        userUpdate.setEmploymentStatus(request.getEmploymentStatus());
        userUpdate.setUpdateBy(userId);
        userUpdate.setAvatar(request.getAvatar());

        sysUserMapper.updateById(userUpdate);

        return buildUserProfileVO(requireCurrentUser());
    }

    @Override
    public List<TemplatesVO> listRecommendTemplates(Integer limit) {
        requireCurrentUser();
        return templatesService.listRecommendTemplatesForCurrentUser(limit);
    }

    private SysUserEntity requireCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserEntity currentUser = sysUserMapper.selectOne(Wrappers.lambdaQuery(SysUserEntity.class)
                .eq(SysUserEntity::getId, userId)
                .eq(SysUserEntity::getIsDeleted, 0)
                .last("limit 1"));
        BizAssert.notNull(currentUser, UserErrorCode.CURRENT_USER_NOT_FOUND);
        return currentUser;
    }

    private UserProfileVO buildUserProfileVO(SysUserEntity user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .employmentStatus(user.getEmploymentStatus())
                .status(user.getStatus())
                .build();
    }
}
