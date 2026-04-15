package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.domain.dto.resume.*;
import cn.chenmanman.resume.domain.vo.resume.MyResumesVO;
import cn.chenmanman.resume.domain.vo.resume.ResumeOptimizeResult;
import cn.chenmanman.resume.domain.vo.resume.ResumePdfVO;
import cn.chenmanman.resume.domain.vo.resume.ResumesVO;
import cn.chenmanman.resume.service.IResumesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/pp/{resumeId}")
    public Result<ResumesVO> getResumeDetailPdf(@PathVariable Long resumeId) {
        return Result.success(resumesService.getResumeDetailNoLogin(resumeId));
    }


    @PostMapping("/rename")
    public Result<Void> renameResume(@RequestBody @Valid RenameResumeRequest request) {
        resumesService.renameResume(request);
        return Result.success();
    }

    @DeleteMapping("/remove")
    public Result<Void> removeResume(Long resumeId) {
        resumesService.removeResume(resumeId);
        return Result.success();
    }
    @GetMapping("/my")
    public Result<List<MyResumesVO>> listResumesMe() {
        return Result.success(resumesService.listResumesMe());
    }

    @GetMapping("/my/page")
    public Result<PageResult<MyResumesVO>> pageResumesMe(@ModelAttribute PageRequest pageRequest) {
        return Result.success(resumesService.pageResumesMe(pageRequest));
    }

    @PutMapping("/{resumeId}/draft")
    public Result<ResumesVO> updateDraft(@PathVariable Long resumeId,
                                         @RequestBody @Valid UpdateResumesDraftRequestPut request) {
        return Result.success(resumesService.updateDraft(resumeId, request));
    }

    @Async
    @PostMapping("/{resumeId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long resumeId) {
        ResumePdfVO pdf = resumesService.exportPdf(resumeId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(pdf.getResumeName()).build()
        );
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf.getResume());
    }

    @PostMapping("/{resumeId}/export/png")
    public Result<Void> exportPng(@PathVariable Long resumeId,
                                  @RequestBody(required = false) @Valid ExportResumePngRequestPost request) {
        resumesService.exportPng(resumeId, request);
        return Result.success();
    }


    @PostMapping("/optimize")
    public Result<ResumeOptimizeResult> optimize(@RequestBody ResumeOptimizeRequest request) {

        return Result.success(resumesService.optimize(request));
    }
}
