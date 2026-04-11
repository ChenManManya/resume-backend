package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.domain.dto.resume.CreateResumesRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePdfRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePngRequestPost;
import cn.chenmanman.resume.domain.dto.resume.UpdateResumesDraftRequestPut;
import cn.chenmanman.resume.domain.vo.resume.MyResumesVO;
import cn.chenmanman.resume.domain.vo.resume.ResumesVO;
import cn.chenmanman.resume.service.IResumesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/resumes")
public class ResumesController {

    private final IResumesService resumesService;

    @PostMapping
    public Result<ResumesVO> createResume(@RequestBody @Valid CreateResumesRequestPost request) {
        return Result.success(resumesService.createResume(request));
    }

    @GetMapping("/{resumeId}")
    public Result<ResumesVO> getResumeDetail(@PathVariable Long resumeId) {
        return Result.success(resumesService.getResumeDetail(resumeId));
    }
    @GetMapping("/my")
    public Result<List<MyResumesVO>> listResumesMe() {
        return Result.success(resumesService.listResumesMe());
    }

    @PutMapping("/{resumeId}/draft")
    public Result<ResumesVO> updateDraft(@PathVariable Long resumeId,
                                         @RequestBody @Valid UpdateResumesDraftRequestPut request) {
        return Result.success(resumesService.updateDraft(resumeId, request));
    }

    @PostMapping("/{resumeId}/export/pdf")
    public Result<Void> exportPdf(@PathVariable Long resumeId,
                                  @RequestBody(required = false) @Valid ExportResumePdfRequestPost request) {
        resumesService.exportPdf(resumeId, request);
        return Result.success();
    }

    @PostMapping("/{resumeId}/export/png")
    public Result<Void> exportPng(@PathVariable Long resumeId,
                                  @RequestBody(required = false) @Valid ExportResumePngRequestPost request) {
        resumesService.exportPng(resumeId, request);
        return Result.success();
    }
}
