package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.exception.BusinessException;
import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.domain.dto.resume.CreateResumeVersionsRequestPost;
import cn.chenmanman.resume.domain.dto.resume.CreateResumesRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePdfRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePngRequestPost;
import cn.chenmanman.resume.domain.dto.resume.UpdateResumesDraftRequestPut;
import cn.chenmanman.resume.domain.entity.resume.ResumeVersionsEntity;
import cn.chenmanman.resume.domain.entity.resume.ResumesEntity;
import cn.chenmanman.resume.domain.entity.resume.TemplatesEntity;
import cn.chenmanman.resume.domain.vo.resume.ResumeVersionsVO;
import cn.chenmanman.resume.domain.vo.resume.ResumesVO;
import cn.chenmanman.resume.mapper.ResumeVersionsMapper;
import cn.chenmanman.resume.mapper.ResumesMapper;
import cn.chenmanman.resume.mapper.TemplatesMapper;
import cn.chenmanman.resume.service.IResumesService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumesServiceImpl implements IResumesService {

    private final ResumesMapper resumesMapper;
    private final ResumeVersionsMapper resumeVersionsMapper;
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
                .createBy(userId)
                .updateBy(userId)
                .build();
        resumesMapper.insert(resume);

        ResumeVersionsEntity version = ResumeVersionsEntity.builder()
                .resumeId(resume.getId())
                .title(request.getTitle())
                .templateId(template.getId())
                .versionNo(1)
                .contentJson(toJsonStorage(template.getSchemaJson()))
                .layoutJson(toJsonStorage(template.getStyleJson()))
                .changeNote("初版")
                .createBy(userId)
                .updateBy(userId)
                .build();
        resumeVersionsMapper.insert(version);

        ResumesEntity resumeUpdate = new ResumesEntity();
        resumeUpdate.setId(resume.getId());
        resumeUpdate.setCurrentVersionId(version.getId());
        resumeUpdate.setUpdateBy(userId);
        resumesMapper.updateById(resumeUpdate);

        return buildResumeVO(requireOwnedResume(resume.getId(), userId), requireResumeVersion(resume.getId(), version.getId()));
    }

    @Override
    public ResumesVO getResumeDetail(Long resumeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        ResumeVersionsEntity version = requireCurrentVersion(resume);
        return buildResumeVO(resume, version);
    }

    @Transactional
    @Override
    public ResumesVO updateDraft(Long resumeId, UpdateResumesDraftRequestPut request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        TemplatesEntity template = requireActiveTemplate(request.getTemplateId());
        ResumeVersionsEntity currentVersion = requireCurrentVersion(resume);

        ResumeVersionsEntity versionUpdate = new ResumeVersionsEntity();
        versionUpdate.setId(currentVersion.getId());
        versionUpdate.setTitle(request.getTitle());
        versionUpdate.setTemplateId(template.getId());
        versionUpdate.setContentJson(toJsonStorage(request.getContentJson()));
        versionUpdate.setLayoutJson(toJsonStorage(request.getLayoutJson()));
        versionUpdate.setUpdateBy(userId);
        resumeVersionsMapper.updateById(versionUpdate);

        ResumesEntity resumeUpdate = new ResumesEntity();
        resumeUpdate.setId(resume.getId());
        resumeUpdate.setTitle(request.getTitle());
        resumeUpdate.setTemplateId(template.getId());
        resumeUpdate.setStatus("draft");
        resumeUpdate.setUpdateBy(userId);
        resumesMapper.updateById(resumeUpdate);

        return getResumeDetail(resumeId);
    }

    @Transactional
    @Override
    public ResumeVersionsVO createVersion(Long resumeId, CreateResumeVersionsRequestPost request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        TemplatesEntity template = requireActiveTemplate(request.getTemplateId());
        ResumeVersionsEntity latestVersion = getLatestVersion(resumeId);
        int nextVersionNo = latestVersion == null ? 1 : latestVersion.getVersionNo() + 1;

        ResumeVersionsEntity version = ResumeVersionsEntity.builder()
                .resumeId(resumeId)
                .title(request.getTitle())
                .templateId(template.getId())
                .versionNo(nextVersionNo)
                .contentJson(toJsonStorage(request.getContentJson()))
                .layoutJson(toJsonStorage(request.getLayoutJson()))
                .changeNote(request.getChangeNote())
                .createBy(userId)
                .updateBy(userId)
                .build();
        resumeVersionsMapper.insert(version);

        ResumesEntity resumeUpdate = new ResumesEntity();
        resumeUpdate.setId(resume.getId());
        resumeUpdate.setTitle(request.getTitle());
        resumeUpdate.setTemplateId(template.getId());
        resumeUpdate.setCurrentVersionId(version.getId());
        resumeUpdate.setStatus("draft");
        resumeUpdate.setUpdateBy(userId);
        resumesMapper.updateById(resumeUpdate);

        return buildResumeVersionDetailVO(requireResumeVersion(resumeId, version.getId()));
    }

    @Override
    public List<ResumeVersionsVO> listVersions(Long resumeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        requireOwnedResume(resumeId, userId);

        LambdaQueryWrapper<ResumeVersionsEntity> queryWrapper = new LambdaQueryWrapper<ResumeVersionsEntity>()
                .eq(ResumeVersionsEntity::getResumeId, resumeId)
                .eq(ResumeVersionsEntity::getIsDeleted, 0)
                .orderByDesc(ResumeVersionsEntity::getVersionNo);

        return resumeVersionsMapper.selectList(queryWrapper).stream()
                .map(this::buildResumeVersionListVO)
                .toList();
    }

    @Transactional
    @Override
    public ResumesVO switchCurrentVersion(Long resumeId, Long versionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        ResumeVersionsEntity version = requireResumeVersion(resumeId, versionId);

        ResumesEntity resumeUpdate = new ResumesEntity();
        resumeUpdate.setId(resume.getId());
        resumeUpdate.setTitle(version.getTitle());
        resumeUpdate.setTemplateId(version.getTemplateId());
        resumeUpdate.setCurrentVersionId(version.getId());
        resumeUpdate.setStatus("draft");
        resumeUpdate.setUpdateBy(userId);
        resumesMapper.updateById(resumeUpdate);

        return buildResumeVO(requireOwnedResume(resumeId, userId), version);
    }

    @Override
    public void exportPdf(Long resumeId, ExportResumePdfRequestPost request) {
        validateExportRequest(resumeId, request == null ? null : request.getVersionId());
        BizAssert.fail(ResumesErrorCode.RESUME_EXPORT_NOT_SUPPORTED);
    }

    @Override
    public void exportPng(Long resumeId, ExportResumePngRequestPost request) {
        validateExportRequest(resumeId, request == null ? null : request.getVersionId());
        BizAssert.fail(ResumesErrorCode.RESUME_EXPORT_NOT_SUPPORTED);
    }

    private void validateExportRequest(Long resumeId, Long versionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        Long targetVersionId = versionId == null ? resume.getCurrentVersionId() : versionId;
        requireResumeVersion(resumeId, targetVersionId);
    }

    private TemplatesEntity requireActiveTemplate(Long templateId) {
        TemplatesEntity template = templatesMapper.selectById(templateId);
        BizAssert.notNull(template, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsDeleted(), 0, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsActive(), 1, ResumesErrorCode.TEMPLATE_DISABLED);
        return template;
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

    private ResumeVersionsEntity requireCurrentVersion(ResumesEntity resume) {
        BizAssert.notNull(resume.getCurrentVersionId(), ResumesErrorCode.RESUME_CURRENT_VERSION_NOT_FOUND);
        return requireResumeVersion(resume.getId(), resume.getCurrentVersionId());
    }

    private ResumeVersionsEntity requireResumeVersion(Long resumeId, Long versionId) {
        BizAssert.notNull(versionId, ResumesErrorCode.RESUME_VERSION_NOT_FOUND);
        LambdaQueryWrapper<ResumeVersionsEntity> queryWrapper = new LambdaQueryWrapper<ResumeVersionsEntity>()
                .eq(ResumeVersionsEntity::getId, versionId)
                .eq(ResumeVersionsEntity::getResumeId, resumeId)
                .eq(ResumeVersionsEntity::getIsDeleted, 0)
                .last("limit 1");
        ResumeVersionsEntity version = resumeVersionsMapper.selectOne(queryWrapper);
        BizAssert.notNull(version, ResumesErrorCode.RESUME_VERSION_NOT_FOUND);
        return version;
    }

    private ResumeVersionsEntity getLatestVersion(Long resumeId) {
        LambdaQueryWrapper<ResumeVersionsEntity> queryWrapper = new LambdaQueryWrapper<ResumeVersionsEntity>()
                .eq(ResumeVersionsEntity::getResumeId, resumeId)
                .eq(ResumeVersionsEntity::getIsDeleted, 0)
                .orderByDesc(ResumeVersionsEntity::getVersionNo)
                .last("limit 1");
        return resumeVersionsMapper.selectOne(queryWrapper);
    }

    private ResumesVO buildResumeVO(ResumesEntity resume, ResumeVersionsEntity version) {
        return ResumesVO.builder()
                .resumeId(resume.getId())
                .currentVersionId(version.getId())
                .versionNo(version.getVersionNo())
                .title(version.getTitle() == null ? resume.getTitle() : version.getTitle())
                .templateId(version.getTemplateId() == null ? resume.getTemplateId() : version.getTemplateId())
                .status(resume.getStatus())
                .contentJson(fromJsonStorage(version.getContentJson()))
                .layoutJson(fromJsonStorage(version.getLayoutJson()))
                .build();
    }

    private ResumeVersionsVO buildResumeVersionDetailVO(ResumeVersionsEntity version) {
        return ResumeVersionsVO.builder()
                .versionId(version.getId())
                .resumeId(version.getResumeId())
                .versionNo(version.getVersionNo())
                .changeNote(version.getChangeNote())
                .contentJson(fromJsonStorage(version.getContentJson()))
                .layoutJson(fromJsonStorage(version.getLayoutJson()))
                .createTime(version.getCreateTime())
                .build();
    }

    private ResumeVersionsVO buildResumeVersionListVO(ResumeVersionsEntity version) {
        return ResumeVersionsVO.builder()
                .versionId(version.getId())
                .resumeId(version.getResumeId())
                .versionNo(version.getVersionNo())
                .changeNote(version.getChangeNote())
                .createTime(version.getCreateTime())
                .build();
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
