package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.domain.entity.resume.TemplatesEntity;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.mapper.TemplatesMapper;
import cn.chenmanman.resume.service.ITemplatesService;
import cn.chenmanman.resume.utils.BizAssert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TemplatesServiceImpl implements ITemplatesService {

    private final TemplatesMapper templatesMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<TemplatesVO> listTemplates() {
        LambdaQueryWrapper<TemplatesEntity> queryWrapper = new LambdaQueryWrapper<TemplatesEntity>()
                .eq(TemplatesEntity::getIsDeleted, 0)
                .eq(TemplatesEntity::getIsActive, 1)
                .orderByAsc(TemplatesEntity::getId);

        return templatesMapper.selectList(queryWrapper).stream()
                .map(this::buildTemplateListVO)
                .toList();
    }

    @Override
    public TemplatesVO getTemplateDetail(Long templateId) {
        TemplatesEntity template = requireActiveTemplate(templateId);
        return buildTemplateDetailVO(template);
    }

    private TemplatesEntity requireActiveTemplate(Long templateId) {
        TemplatesEntity template = templatesMapper.selectById(templateId);
        BizAssert.notNull(template, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsDeleted(), 0, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsActive(), 1, ResumesErrorCode.TEMPLATE_DISABLED);
        return template;
    }

    private TemplatesVO buildTemplateListVO(TemplatesEntity template) {
        return TemplatesVO.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .description(template.getDescription())
                .previewImageUrl(template.getPreviewImageUrl())
                .category(template.getCategory())
                .build();
    }

    private TemplatesVO buildTemplateDetailVO(TemplatesEntity template) {
        return TemplatesVO.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .description(template.getDescription())
                .previewImageUrl(template.getPreviewImageUrl())
                .category(template.getCategory())
                .schemaJson(fromJsonStorage(template.getSchemaJson()))
                .styleJson(fromJsonStorage(template.getStyleJson()))
                .build();
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
            log.warn("Failed to parse template json", e);
            return stringValue;
        }
    }
}
