package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.common.exception.BusinessException;
import cn.chenmanman.resume.domain.dto.resume.CreateResumesRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePdfRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePngRequestPost;
import cn.chenmanman.resume.domain.dto.resume.UpdateResumesDraftRequestPut;
import cn.chenmanman.resume.domain.entity.resume.ResumesEntity;
import cn.chenmanman.resume.domain.entity.resume.TemplatesEntity;
import cn.chenmanman.resume.domain.vo.resume.MyResumesVO;
import cn.chenmanman.resume.domain.vo.resume.ResumesVO;
import cn.chenmanman.resume.mapper.ResumesMapper;
import cn.chenmanman.resume.mapper.TemplatesMapper;
import cn.chenmanman.resume.service.IResumesService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumesServiceImpl implements IResumesService {

    private final ResumesMapper resumesMapper;
    private final TemplatesMapper templatesMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public ResumesVO createResume(CreateResumesRequestPost request) {
        Long userId = StpUtil.getLoginIdAsLong();
        TemplatesEntity template = requireActiveTemplate(request.getTemplateId());

        ResumesEntity resume = ResumesEntity.builder()
                .userId(userId)
                .title(request.getTitle())
                .templateId(template.getId())
                .status("draft")
                .contentJson(toJsonStorage(resolveInitialContent(template)))
                .layoutJson(toJsonStorage(resolveInitialLayout(template)))
                .createBy(userId)
                .updateBy(userId)
                .build();
        resumesMapper.insert(resume);
        incrementTemplateUsage(template.getId());

        return getResumeDetail(resume.getId());
    }

    @Override
    public ResumesVO getResumeDetail(Long resumeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        return buildResumeVO(resume);
    }

    @Transactional
    @Override
    public ResumesVO updateDraft(Long resumeId, UpdateResumesDraftRequestPut request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        TemplatesEntity template = requireActiveTemplate(request.getTemplateId());
        Long previousTemplateId = resume.getTemplateId();

        ResumesEntity resumeUpdate = new ResumesEntity();
        resumeUpdate.setId(resume.getId());
        resumeUpdate.setTitle(request.getTitle());
        resumeUpdate.setTemplateId(template.getId());
        resumeUpdate.setStatus("draft");
        resumeUpdate.setContentJson(toJsonStorage(request.getContentJson()));
        resumeUpdate.setLayoutJson(toJsonStorage(request.getLayoutJson()));
        resumeUpdate.setUpdateBy(userId);
        resumesMapper.updateById(resumeUpdate);

        if (previousTemplateId == null || !previousTemplateId.equals(template.getId())) {
            incrementTemplateUsage(template.getId());
        }

        return getResumeDetail(resumeId);
    }

    @Override
    public void exportPdf(Long resumeId, ExportResumePdfRequestPost request) {
        validateExportAccess(resumeId);
        BizAssert.fail(ResumesErrorCode.RESUME_EXPORT_NOT_SUPPORTED);
    }

    @Override
    public void exportPng(Long resumeId, ExportResumePngRequestPost request) {
        validateExportAccess(resumeId);
        BizAssert.fail(ResumesErrorCode.RESUME_EXPORT_NOT_SUPPORTED);
    }

    @Override
    public List<MyResumesVO> listResumesMe() {
        Long userId = StpUtil.getLoginIdAsLong();

        return resumesMapper.getResumeMeList(userId);
    }

    @Override
    public PageResult<MyResumesVO> pageResumesMe(PageRequest pageRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageRequest currentPageRequest = pageRequest == null ? new PageRequest() : pageRequest;
        int currentPageNum = currentPageRequest.getSafePageNum();
        int currentPageSize = currentPageRequest.getSafePageSize();

        Page<MyResumesVO> page = resumesMapper.getResumeMePage(new Page<>(currentPageNum, currentPageSize), userId);
        PageResult<MyResumesVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(currentPageNum);
        pageResult.setPageSize(currentPageSize);
        pageResult.setList(page.getRecords());
        return pageResult;
    }

    private void validateExportAccess(Long resumeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        requireOwnedResume(resumeId, userId);
    }

    private TemplatesEntity requireActiveTemplate(Long templateId) {
        TemplatesEntity template = templatesMapper.selectById(templateId);
        BizAssert.notNull(template, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsDeleted(), 0, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsActive(), 1, ResumesErrorCode.TEMPLATE_DISABLED);
        return template;
    }

    private void incrementTemplateUsage(Long templateId) {
        templatesMapper.update(null, Wrappers.<TemplatesEntity>lambdaUpdate()
                .eq(TemplatesEntity::getId, templateId)
                .setSql("usage_number = IFNULL(usage_number, 0) + 1"));
    }

    private ResumesEntity requireOwnedResume(Long resumeId, Long userId) {
        LambdaQueryWrapper<ResumesEntity> queryWrapper = new LambdaQueryWrapper<ResumesEntity>()
                .eq(ResumesEntity::getId, resumeId)
                .eq(ResumesEntity::getUserId, userId)
                .eq(ResumesEntity::getIsDeleted, 0)
                .last("limit 1");
        ResumesEntity resume = resumesMapper.selectOne(queryWrapper);
        BizAssert.notNull(resume, ResumesErrorCode.RESUME_NOT_FOUND);
        return resume;
    }

    private ResumesVO buildResumeVO(ResumesEntity resume) {
        return ResumesVO.builder()
                .resumeId(resume.getId())
                .title(resume.getTitle())
                .templateId(resume.getTemplateId())
                .status(resume.getStatus())
                .contentJson(fromJsonStorage(resume.getContentJson()))
                .layoutJson(fromJsonStorage(resume.getLayoutJson()))
                .build();
    }

    private Object resolveInitialContent(TemplatesEntity template) {
        return resolveTemplateJson(template.getDefaultContentJson(), template.getSchemaJson());
    }

    private Object resolveInitialLayout(TemplatesEntity template) {
        return resolveTemplateJson(template.getStyleJson(), template.getSchemaJson());
    }

    private Object resolveTemplateJson(Object preferredValue, Object fallbackValue) {
        Object resolvedValue = fromJsonStorage(preferredValue);
        if (resolvedValue != null) {
            return resolvedValue;
        }

        resolvedValue = fromJsonStorage(fallbackValue);
        if (resolvedValue != null) {
            return resolvedValue;
        }

        return Collections.emptyMap();
    }

    private String toJsonStorage(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON序列化失败");
        }
    }

    private Object fromJsonStorage(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String stringValue)) {
            return value;
        }
        try {
            return objectMapper.readValue(stringValue, Object.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse resume json", e);
            return stringValue;
        }
    }
}
