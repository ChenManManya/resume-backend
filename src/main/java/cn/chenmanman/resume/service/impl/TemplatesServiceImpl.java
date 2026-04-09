package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.domain.dto.template.TemplateMatchPageRequest;
import cn.chenmanman.resume.domain.entity.resume.TemplatesEntity;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.mapper.TemplatesMapper;
import cn.chenmanman.resume.service.ITemplatesService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public Map<String, List<String>> getTemplateTagGroups() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, LinkedHashSet<String>> tempMap = new LinkedHashMap<>();

        List<TemplatesEntity> templates = templatesMapper.selectList(
                Wrappers.<TemplatesEntity>lambdaQuery()
                        .select(TemplatesEntity::getCategory, TemplatesEntity::getTags)
        );

        for (TemplatesEntity template : templates) {
            List<String> tagList = new ArrayList<>();
            Object tagsObj = template.getTags();
            if (tagsObj != null) {
                if (tagsObj instanceof List<?>) {
                    // 如果是已经是 List<Object>
                    tagList = ((List<?>) tagsObj).stream()
                            .map(String::valueOf)
                            .toList();
                } else {
                    // 如果是 JSON 字符串
                    try {
                        tagList = objectMapper.readValue(
                                tagsObj.toString(),
                                new TypeReference<List<String>>() {}
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            tempMap.computeIfAbsent(template.getCategory(), k -> new LinkedHashSet<>())
                    .addAll(tagList);
        }

        Map<String, List<String>> tagGroups = new LinkedHashMap<>();
        tempMap.forEach((category, tags) -> tagGroups.put(category, new ArrayList<>(tags)));
        return tagGroups;
    }

    @Override
    public PageResult<TemplatesVO> pageTemplates(TemplateMatchPageRequest pageRequest) {
        TemplateMatchPageRequest currentPageRequest = pageRequest == null ? new TemplateMatchPageRequest() : pageRequest;
        int currentPageNum = currentPageRequest.getSafePageNum();
        int currentPageSize = currentPageRequest.getSafePageSize();

        LambdaQueryWrapper<TemplatesEntity> queryWrapper = new LambdaQueryWrapper<TemplatesEntity>()
                .eq(TemplatesEntity::getIsDeleted, 0)
                .eq(TemplatesEntity::getIsActive, 1)
                .orderByAsc(TemplatesEntity::getId);

        if (StringUtils.hasText(currentPageRequest.getCategory())) {
            queryWrapper.eq(TemplatesEntity::getCategory, currentPageRequest.getCategory().trim());
        }
        if (!CollectionUtils.isEmpty(currentPageRequest.getTags())) {
            List<String> validTags = currentPageRequest.getTags().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList();
            if (!validTags.isEmpty()) {
                queryWrapper.and(wrapper -> {
                    for (int i = 0; i < validTags.size(); i++) {
                        String tag = validTags.get(i);
                        if (i == 0) {
                            wrapper.apply("JSON_CONTAINS(tags, JSON_ARRAY({0}))", tag);
                        } else {
                            wrapper.or().apply("JSON_CONTAINS(tags, JSON_ARRAY({0}))", tag);
                        }
                    }
                });
            }
        }

        Page<TemplatesEntity> page = templatesMapper.selectPage(new Page<>(currentPageNum, currentPageSize), queryWrapper);

        PageResult<TemplatesVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(currentPageNum);
        pageResult.setPageSize(currentPageSize);
        pageResult.setList(page.getRecords().stream()
                .map(this::buildTemplateListVO)
                .toList());
        return pageResult;
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
                .tags(fromJsonStorage(template.getTags()))
                .usageNumber(template.getUsageNumber())
                .defaultContentJson(template.getDefaultContentJson())
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
                .tags(fromJsonStorage(template.getTags()))
                .usageNumber(template.getUsageNumber())
                .schemaJson(fromJsonStorage(template.getSchemaJson()))
                .styleJson(fromJsonStorage(template.getStyleJson()))
                .defaultContentJson(template.getDefaultContentJson())
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
