package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.domain.dto.template.GuestTemplateRecommendRequest;
import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.domain.dto.template.TemplateMatchPageRequest;
import cn.chenmanman.resume.domain.entity.resume.ResumesEntity;
import cn.chenmanman.resume.domain.entity.resume.TemplatesEntity;
import cn.chenmanman.resume.domain.entity.resume.UserTemplateFavoriteEntity;
import cn.chenmanman.resume.domain.entity.user.SysUserEntity;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.domain.vo.resume.UserFavoriteVO;
import cn.chenmanman.resume.mapper.TemplatesMapper;
import cn.chenmanman.resume.mapper.ResumesMapper;
import cn.chenmanman.resume.mapper.SysUserMapper;
import cn.chenmanman.resume.mapper.UserTemplateFavoriteMapper;
import cn.chenmanman.resume.service.ITemplatesService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.dev33.satoken.stp.StpUtil;
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
    private final UserTemplateFavoriteMapper userTemplateFavoriteMapper;
    private final ResumesMapper resumesMapper;
    private final SysUserMapper sysUserMapper;

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
    public List<TemplatesVO> listRecommendTemplates(Long templateId, Integer limit) {
        TemplatesEntity sourceTemplate = requireActiveTemplate(templateId);
        int currentLimit = normalizeRecommendLimit(limit);

        List<TemplatesEntity> candidates = templatesMapper.selectList(Wrappers.<TemplatesEntity>lambdaQuery()
                .eq(TemplatesEntity::getIsActive, 1)
                .ne(TemplatesEntity::getId, templateId));

        List<TemplatesEntity> recommendTemplates = candidates.stream()
                .map(template -> new ScoredTemplate(template, scoreTemplate(sourceTemplate, template)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator
                        .comparingInt(ScoredTemplate::score).reversed()
                        .thenComparing(scored -> scored.template().getUsageNumber(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(scored -> scored.template().getId(), Comparator.reverseOrder()))
                .map(ScoredTemplate::template)
                .limit(currentLimit)
                .toList();

        if (recommendTemplates.size() < currentLimit) {
            Set<Long> existingIds = recommendTemplates.stream().map(TemplatesEntity::getId).collect(Collectors.toSet());
            List<TemplatesEntity> hotFallback = candidates.stream()
                    .filter(template -> !existingIds.contains(template.getId()))
                    .sorted(Comparator
                            .comparing(TemplatesEntity::getUsageNumber, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(TemplatesEntity::getId, Comparator.reverseOrder()))
                    .limit(currentLimit - recommendTemplates.size())
                    .toList();
            recommendTemplates = new ArrayList<>(recommendTemplates);
            recommendTemplates.addAll(hotFallback);
        }

        return recommendTemplates.stream()
                .map(this::buildTemplateListVO)
                .toList();
    }

    @Override
    public List<TemplatesVO> listRecommendTemplatesForCurrentUser(Integer limit) {
        long userId = StpUtil.getLoginIdAsLong();
        int currentLimit = normalizeRecommendLimit(limit);
        SysUserEntity currentUser = sysUserMapper.selectById(userId);

        List<UserTemplateFavoriteEntity> favorites = userTemplateFavoriteMapper.selectList(Wrappers.<UserTemplateFavoriteEntity>lambdaQuery()
                .eq(UserTemplateFavoriteEntity::getUserId, userId)
                .eq(UserTemplateFavoriteEntity::getIsDeleted, 0));
        List<ResumesEntity> resumes = resumesMapper.selectList(Wrappers.<ResumesEntity>lambdaQuery()
                .select(ResumesEntity::getTemplateId)
                .eq(ResumesEntity::getUserId, userId)
                .eq(ResumesEntity::getIsDeleted, 0));

        Map<Long, Integer> templateWeights = new HashMap<>();
        Set<Long> interactedTemplateIds = new LinkedHashSet<>();

        for (UserTemplateFavoriteEntity favorite : favorites) {
            if (favorite.getTemplateId() == null) {
                continue;
            }
            interactedTemplateIds.add(favorite.getTemplateId());
            templateWeights.merge(favorite.getTemplateId(), 5, Integer::sum);
        }
        for (ResumesEntity resume : resumes) {
            if (resume.getTemplateId() == null) {
                continue;
            }
            interactedTemplateIds.add(resume.getTemplateId());
            templateWeights.merge(resume.getTemplateId(), 3, Integer::sum);
        }

        List<TemplatesEntity> interactedTemplates = interactedTemplateIds.isEmpty()
                ? List.of()
                : templatesMapper.selectBatchIds(interactedTemplateIds).stream()
                .filter(template -> template.getIsDeleted() != null && template.getIsDeleted() == 0)
                .filter(template -> template.getIsActive() != null && template.getIsActive() == 1)
                .toList();

        Map<String, Integer> categoryWeights = new HashMap<>();
        Map<String, Integer> tagWeights = new HashMap<>();
        Set<String> keywordSet = new LinkedHashSet<>();

        for (TemplatesEntity template : interactedTemplates) {
            int baseWeight = templateWeights.getOrDefault(template.getId(), 1);
            if (StringUtils.hasText(template.getCategory())) {
                categoryWeights.merge(template.getCategory().trim(), baseWeight * 4, Integer::sum);
            }
            for (String tag : toStringSet(fromJsonStorage(template.getTags()))) {
                tagWeights.merge(tag, baseWeight * 5, Integer::sum);
            }
            keywordSet.addAll(extractKeywords(template.getName(), template.getDescription()));
        }

        if (interactedTemplates.isEmpty()) {
            applyColdStartPreference(currentUser == null ? null : currentUser.getEmploymentStatus(), categoryWeights, tagWeights, keywordSet);
        }

        List<TemplatesEntity> candidates = templatesMapper.selectList(Wrappers.<TemplatesEntity>lambdaQuery()
                .eq(TemplatesEntity::getIsDeleted, 0)
                .eq(TemplatesEntity::getIsActive, 1));

        List<TemplatesEntity> recommendTemplates = candidates.stream()
                .filter(template -> !interactedTemplateIds.contains(template.getId()))
                .map(template -> new ScoredTemplate(template, scoreTemplateForUser(template, categoryWeights, tagWeights, keywordSet)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator
                        .comparingInt(ScoredTemplate::score).reversed()
                        .thenComparing(scored -> scored.template().getUsageNumber(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(scored -> scored.template().getId(), Comparator.reverseOrder()))
                .map(ScoredTemplate::template)
                .limit(currentLimit)
                .toList();

        if (recommendTemplates.size() < currentLimit) {
            Set<Long> existingIds = recommendTemplates.stream().map(TemplatesEntity::getId).collect(Collectors.toSet());
            List<TemplatesEntity> hotFallback = candidates.stream()
                    .filter(template -> !existingIds.contains(template.getId()))
                    .filter(template -> !interactedTemplateIds.contains(template.getId()))
                    .sorted(Comparator
                            .comparing(TemplatesEntity::getUsageNumber, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(TemplatesEntity::getId, Comparator.reverseOrder()))
                    .limit(currentLimit - recommendTemplates.size())
                    .toList();
            recommendTemplates = new ArrayList<>(recommendTemplates);
            recommendTemplates.addAll(hotFallback);
        }

        return recommendTemplates.stream().map(this::buildTemplateListVO).toList();
    }

    @Override
    public List<TemplatesVO> listGuestRecommendTemplates(GuestTemplateRecommendRequest request) {
        GuestTemplateRecommendRequest currentRequest = request == null ? new GuestTemplateRecommendRequest() : request;
        int currentLimit = currentRequest.getSafePageSize();
        boolean hasExplicitIntent = StringUtils.hasText(currentRequest.getCategory())
                || StringUtils.hasText(currentRequest.getTag())
                || StringUtils.hasText(currentRequest.getKeyword());

        Map<String, Integer> categoryWeights = new HashMap<>();
        Map<String, Integer> tagWeights = new HashMap<>();
        Set<String> keywordSet = new LinkedHashSet<>();

        if (StringUtils.hasText(currentRequest.getCategory())) {
            categoryWeights.merge(currentRequest.getCategory().trim(), 10, Integer::sum);
        }
        if (StringUtils.hasText(currentRequest.getTag())) {
            tagWeights.merge(currentRequest.getTag().trim(), 12, Integer::sum);
        }
        if (StringUtils.hasText(currentRequest.getKeyword())) {
            keywordSet.addAll(extractKeywords(currentRequest.getKeyword()));
        }
        if (currentRequest.getEmploymentStatus() != null || !hasExplicitIntent) {
            applyColdStartPreference(currentRequest.getEmploymentStatus(), categoryWeights, tagWeights, keywordSet);
        }

        List<TemplatesEntity> candidates = templatesMapper.selectList(Wrappers.<TemplatesEntity>lambdaQuery()
                .eq(TemplatesEntity::getIsDeleted, 0)
                .eq(TemplatesEntity::getIsActive, 1));

        boolean hasRecommendIntent = !categoryWeights.isEmpty() || !tagWeights.isEmpty() || !keywordSet.isEmpty();

        List<TemplatesEntity> recommendTemplates = candidates.stream()
                .map(template -> new ScoredTemplate(template, scoreTemplateForUser(template, categoryWeights, tagWeights, keywordSet)))
                .filter(scored -> hasRecommendIntent ? scored.score() > 0 : true)
                .sorted(Comparator
                        .comparingInt(ScoredTemplate::score).reversed()
                        .thenComparing(scored -> scored.template().getUsageNumber(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(scored -> scored.template().getId(), Comparator.reverseOrder()))
                .map(ScoredTemplate::template)
                .limit(currentLimit)
                .toList();

        if (recommendTemplates.size() < currentLimit) {
            Set<Long> existingIds = recommendTemplates.stream().map(TemplatesEntity::getId).collect(Collectors.toSet());
            List<TemplatesEntity> hotFallback = candidates.stream()
                    .filter(template -> !existingIds.contains(template.getId()))
                    .sorted(Comparator
                            .comparing(TemplatesEntity::getUsageNumber, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(TemplatesEntity::getId, Comparator.reverseOrder()))
                    .limit(currentLimit - recommendTemplates.size())
                    .toList();
            recommendTemplates = new ArrayList<>(recommendTemplates);
            recommendTemplates.addAll(hotFallback);
        }

        return recommendTemplates.stream().map(this::buildTemplateListVO).toList();
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
    public PageResult<UserFavoriteVO> listFavoriteTemplate(PageRequest pageRequest) {
        PageRequest currentPageRequest = pageRequest == null ? new PageRequest() : pageRequest;
        int currentPageNum = currentPageRequest.getSafePageNum();
        int currentPageSize = currentPageRequest.getSafePageSize();
        long userId = StpUtil.getLoginIdAsLong();
        Page<UserFavoriteVO> page = userTemplateFavoriteMapper.selectUserFavoriteTemplate(new Page<>(currentPageNum, currentPageSize),userId);
        PageResult<UserFavoriteVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(currentPageNum);
        pageResult.setPageSize(currentPageSize);
        pageResult.setList(page.getRecords());
        return pageResult;
    }

    @Override
    public void favoriteTemplate(Long templateId) {
        long userId = StpUtil.getLoginIdAsLong();
        Boolean isFavorite = this.isFavoriteTemplate(templateId);
        if (!isFavorite) {
            userTemplateFavoriteMapper.insert(UserTemplateFavoriteEntity.builder().userId(userId).templateId(templateId).build());
        } else {
            userTemplateFavoriteMapper.delete(Wrappers.<UserTemplateFavoriteEntity>lambdaQuery().eq(UserTemplateFavoriteEntity::getTemplateId, templateId).eq(UserTemplateFavoriteEntity::getUserId, userId));
        }

    }

    @Override
    public Boolean isFavoriteTemplate(Long templateId) {
        long userId = StpUtil.getLoginIdAsLong();



        return Objects.nonNull(userTemplateFavoriteMapper.selectOne(Wrappers.<UserTemplateFavoriteEntity>lambdaQuery().eq(UserTemplateFavoriteEntity::getTemplateId, templateId).eq(UserTemplateFavoriteEntity::getUserId, userId)));
    }

    @Override
    public List<String> getTemplateCategory() {
        return templatesMapper.selectTemplateCategory();
    }

    @Override
    public PageResult<TemplatesVO> pageTemplates(TemplateMatchPageRequest pageRequest) {
        TemplateMatchPageRequest currentPageRequest = pageRequest == null ? new TemplateMatchPageRequest() : pageRequest;
        int currentPageNum = currentPageRequest.getSafePageNum();
        int currentPageSize = currentPageRequest.getSafePageSize();

        LambdaQueryWrapper<TemplatesEntity> queryWrapper = new LambdaQueryWrapper<TemplatesEntity>()
                .eq(TemplatesEntity::getIsActive, 1)
                .orderByAsc(TemplatesEntity::getId);

        if (StringUtils.hasText(currentPageRequest.getCategory())) {
            queryWrapper.eq(TemplatesEntity::getCategory, currentPageRequest.getCategory().trim());
        }
        if (StringUtils.hasText(currentPageRequest.getTag())) {
            queryWrapper.and(wrapper -> {
                wrapper.apply("JSON_CONTAINS(tags, JSON_QUOTE({0}))", currentPageRequest.getTag().trim());
            });
        }
        if (StringUtils.hasText(currentPageRequest.getKeyword())) {
            String keyword = currentPageRequest.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like(TemplatesEntity::getName, keyword)
                    .or()
                    .like(TemplatesEntity::getDescription, keyword));
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

    private int scoreTemplate(TemplatesEntity sourceTemplate, TemplatesEntity candidate) {
        int score = 0;
        if (Objects.equals(sourceTemplate.getCategory(), candidate.getCategory())) {
            score += 8;
        }

        Set<String> sourceTags = toStringSet(fromJsonStorage(sourceTemplate.getTags()));
        Set<String> candidateTags = toStringSet(fromJsonStorage(candidate.getTags()));
        for (String tag : sourceTags) {
            if (candidateTags.contains(tag)) {
                score += 10;
            }
        }

        Set<String> keywords = extractKeywords(sourceTemplate.getName(), sourceTemplate.getDescription());
        String candidateText = (nullToEmpty(candidate.getName()) + " " + nullToEmpty(candidate.getDescription())).toLowerCase();
        for (String keyword : keywords) {
            if (candidateText.contains(keyword)) {
                score += 3;
            }
        }

        Integer usageNumber = candidate.getUsageNumber();
        if (usageNumber != null) {
            score += Math.min(usageNumber / 10, 20);
        }
        return score;
    }

    private int scoreTemplateForUser(TemplatesEntity candidate,
                                     Map<String, Integer> categoryWeights,
                                     Map<String, Integer> tagWeights,
                                     Set<String> keywordSet) {
        int score = 0;

        if (StringUtils.hasText(candidate.getCategory())) {
            score += categoryWeights.getOrDefault(candidate.getCategory().trim(), 0);
        }

        for (String tag : toStringSet(fromJsonStorage(candidate.getTags()))) {
            score += tagWeights.getOrDefault(tag, 0);
        }

        String candidateText = (nullToEmpty(candidate.getName()) + " " + nullToEmpty(candidate.getDescription())).toLowerCase();
        for (String keyword : keywordSet) {
            if (candidateText.contains(keyword)) {
                score += 2;
            }
        }

        Integer usageNumber = candidate.getUsageNumber();
        if (usageNumber != null) {
            score += Math.min(usageNumber / 10, 20);
        }

        return score;
    }

    private void applyColdStartPreference(Integer employmentStatus,
                                          Map<String, Integer> categoryWeights,
                                          Map<String, Integer> tagWeights,
                                          Set<String> keywordSet) {
        if (employmentStatus == null) {
            tagWeights.merge("校招", 6, Integer::sum);
            tagWeights.merge("实习", 4, Integer::sum);
            keywordSet.add("校招");
            keywordSet.add("实习");
            return;
        }

        switch (employmentStatus) {
            case 0 -> {
                tagWeights.merge("社招", 8, Integer::sum);
                categoryWeights.merge("通用", 3, Integer::sum);
                keywordSet.add("社招");
            }
            case 1 -> {
                tagWeights.merge("校招", 8, Integer::sum);
                tagWeights.merge("应届", 4, Integer::sum);
                categoryWeights.merge("通用", 3, Integer::sum);
                keywordSet.add("校招");
                keywordSet.add("应届");
            }
            case 2 -> {
                tagWeights.merge("实习", 8, Integer::sum);
                tagWeights.merge("校招", 4, Integer::sum);
                categoryWeights.merge("通用", 3, Integer::sum);
                keywordSet.add("实习");
                keywordSet.add("校招");
            }
            case 3 -> {
                tagWeights.merge("通用", 4, Integer::sum);
                tagWeights.merge("校招", 3, Integer::sum);
                tagWeights.merge("社招", 3, Integer::sum);
                categoryWeights.merge("通用", 4, Integer::sum);
                keywordSet.add("通用");
            }
            default -> {
                tagWeights.merge("通用", 4, Integer::sum);
                categoryWeights.merge("通用", 3, Integer::sum);
                keywordSet.add("通用");
            }
        }
    }

    private Set<String> toStringSet(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return new LinkedHashSet<>();
    }

    private Set<String> extractKeywords(String... texts) {
        Set<String> keywords = new LinkedHashSet<>();
        for (String text : texts) {
            if (!StringUtils.hasText(text)) {
                continue;
            }
            String[] parts = text.toLowerCase().split("[\\s,，。.!！？?、；;:：()（）【】\\[\\]{}《》\"'“”‘’/\\\\|+-]+");
            for (String part : parts) {
                String keyword = part.trim();
                if (keyword.length() >= 2) {
                    keywords.add(keyword);
                }
                if (keywords.size() >= 20) {
                    return keywords;
                }
            }
        }
        return keywords;
    }

    private int normalizeRecommendLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 6;
        }
        return Math.min(limit, 20);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ScoredTemplate(TemplatesEntity template, int score) {
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
