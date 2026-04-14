package cn.chenmanman.resume.service;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.domain.dto.resume.CreateResumesRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePdfRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePngRequestPost;
import cn.chenmanman.resume.domain.dto.resume.UpdateResumesDraftRequestPut;
import cn.chenmanman.resume.domain.vo.resume.MyResumesVO;
import cn.chenmanman.resume.domain.vo.resume.ResumesVO;

import java.util.List;

public interface IResumesService {
    ResumesVO createResume(CreateResumesRequestPost request);

    ResumesVO getResumeDetail(Long resumeId);

    ResumesVO updateDraft(Long resumeId, UpdateResumesDraftRequestPut request);

    void exportPdf(Long resumeId, ExportResumePdfRequestPost request);

    void exportPng(Long resumeId, ExportResumePngRequestPost request);

    List<MyResumesVO> listResumesMe();

    PageResult<MyResumesVO> pageResumesMe(PageRequest pageRequest);

}
