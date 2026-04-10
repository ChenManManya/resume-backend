package cn.chenmanman.resume.service;

import cn.chenmanman.resume.domain.vo.user.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    String uploadResumePhoto(MultipartFile file);

    String uploadUserAvatar(MultipartFile file);
}
