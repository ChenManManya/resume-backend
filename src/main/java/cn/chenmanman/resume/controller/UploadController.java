package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final IUploadService uploadService;

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadResumePhoto( @ModelAttribute MultipartFile file) {
        return Result.success(uploadService.uploadResumePhoto(file));
    }


    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadUserAvatar( @ModelAttribute MultipartFile file) {
        return Result.success(uploadService.uploadUserAvatar(file));
    }

}
