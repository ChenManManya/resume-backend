package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.common.error.UserErrorCode;
import cn.chenmanman.resume.config.LocalUploadProperties;
import cn.chenmanman.resume.service.IUploadService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.chenmanman.resume.utils.LocalFileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadServiceImpl implements IUploadService {

    private final LocalFileUploadUtil  localFileUploadUtil;

    @Override
    public String uploadResumePhoto(MultipartFile file) {
        return localFileUploadUtil.uploadPhoto(file);
    }

}
