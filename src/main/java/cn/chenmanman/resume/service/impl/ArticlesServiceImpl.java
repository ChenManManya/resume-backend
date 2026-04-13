package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.common.error.ArticleErrorCode;
import cn.chenmanman.resume.common.exception.BusinessException;
import cn.chenmanman.resume.domain.dto.article.ArticlePageRequest;
import cn.chenmanman.resume.domain.dto.article.CreateArticleRequestPost;
import cn.chenmanman.resume.domain.dto.article.UpdateArticleRequestPut;
import cn.chenmanman.resume.domain.entity.article.ArticlesEntity;
import cn.chenmanman.resume.domain.vo.article.ArticleDetailVO;
import cn.chenmanman.resume.domain.vo.article.ArticlePageVO;
import cn.chenmanman.resume.mapper.ArticlesMapper;
import cn.chenmanman.resume.service.IArticlesService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.chenmanman.resume.utils.LocalFileUploadUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticlesServiceImpl implements IArticlesService {

    private static final List<Integer> VALID_STATUS = List.of(0, 1, 2);

    private final ArticlesMapper articlesMapper;
    private final ObjectMapper objectMapper;
    private final LocalFileUploadUtil localFileUploadUtil;

    @Transactional
    @Override
    public ArticleDetailVO publishArticle(CreateArticleRequestPost request) {
        Long userId = StpUtil.getLoginIdAsLong();
        validateArticleRequest(request.getTitle(), request.getContent(), request.getTags(), request.getStatus());

        ArticlesEntity article = ArticlesEntity.builder()
                .coverUrl(trimToNull(request.getCoverUrl()))
                .title(request.getTitle().trim())
                .content(request.getContent())
                .tags(toJsonStorage(normalizeTags(request.getTags())))
                .viewNum(0)
                .status(request.getStatus())
                .publishedTime(resolvePublishedTime(null, request.getStatus()))
                .createBy(userId)
                .updateBy(userId)
                .deleted(0)
                .build();
        articlesMapper.insert(article);
        return getArticleDetail(article.getId());
    }

    @Transactional
    @Override
    public ArticleDetailVO updateArticle(Long articleId, UpdateArticleRequestPut request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ArticlesEntity article = requireOwnedArticle(articleId, userId);
        validateArticleRequest(request.getTitle(), request.getContent(), request.getTags(), request.getStatus());
        String oldCoverUrl = article.getCoverUrl();
        String newCoverUrl = trimToNull(request.getCoverUrl());

        ArticlesEntity articleUpdate = ArticlesEntity.builder()
                .id(article.getId())
                .coverUrl(newCoverUrl)
                .title(request.getTitle().trim())
                .content(request.getContent())
                .tags(toJsonStorage(normalizeTags(request.getTags())))
                .status(request.getStatus())
                .publishedTime(resolvePublishedTime(article.getPublishedTime(), request.getStatus()))
                .updateBy(userId)
                .build();
        articlesMapper.updateById(articleUpdate);
        deleteOldCoverIfReplaced(oldCoverUrl, newCoverUrl);
        return getArticleDetail(articleId);
    }

    @Transactional
    @Override
    public void deleteArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ArticlesEntity article = requireOwnedArticle(articleId, userId);

        ArticlesEntity articleUpdate = ArticlesEntity.builder()
                .id(article.getId())
                .deleted(1)
                .updateBy(userId)
                .build();
        articlesMapper.updateById(articleUpdate);
        localFileUploadUtil.deleteByPublicUrl(article.getCoverUrl());
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ArticlesEntity article = requireOwnedArticle(articleId, userId);
        return buildArticleDetailVO(article);
    }

    @Override
    public ArticleDetailVO getPublicArticleDetail(Long articleId) {
        return buildArticleDetailVO(requirePublishedArticle(articleId));
    }

    @Override
    public PageResult<ArticlePageVO> pageArticles(ArticlePageRequest request) {
        ArticlePageRequest currentRequest = request == null ? new ArticlePageRequest() : request;
        int currentPageNum = currentRequest.getSafePageNum();
        int currentPageSize = currentRequest.getSafePageSize();
        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<ArticlesEntity> queryWrapper = new LambdaQueryWrapper<ArticlesEntity>()
                .eq(ArticlesEntity::getDeleted, 0)
                .eq(ArticlesEntity::getCreateBy, userId)
                .orderByDesc(ArticlesEntity::getPublishedTime)
                .orderByDesc(ArticlesEntity::getCreateTime)
                .orderByDesc(ArticlesEntity::getId);

        if (currentRequest.getStatus() != null) {
            validateStatus(currentRequest.getStatus());
            queryWrapper.eq(ArticlesEntity::getStatus, currentRequest.getStatus());
        }
        if (StringUtils.hasText(currentRequest.getTag())) {
            queryWrapper.apply("JSON_CONTAINS(tags, JSON_QUOTE({0}))", currentRequest.getTag().trim());
        }
        if (StringUtils.hasText(currentRequest.getKeyword())) {
            String keyword = currentRequest.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like(ArticlesEntity::getTitle, keyword)
                    .or()
                    .like(ArticlesEntity::getContent, keyword));
        }

        Page<ArticlesEntity> page = articlesMapper.selectPage(new Page<>(currentPageNum, currentPageSize), queryWrapper);

        PageResult<ArticlePageVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(currentPageNum);
        pageResult.setPageSize(currentPageSize);
        pageResult.setList(page.getRecords().stream().map(this::buildArticlePageVO).toList());
        return pageResult;
    }

    @Override
    public PageResult<ArticlePageVO> pagePublicArticles(ArticlePageRequest request) {
        ArticlePageRequest currentRequest = request == null ? new ArticlePageRequest() : request;
        int currentPageNum = currentRequest.getSafePageNum();
        int currentPageSize = currentRequest.getSafePageSize();

        LambdaQueryWrapper<ArticlesEntity> queryWrapper = new LambdaQueryWrapper<ArticlesEntity>()
                .eq(ArticlesEntity::getDeleted, 0)
                .eq(ArticlesEntity::getStatus, 1)
                .orderByDesc(ArticlesEntity::getPublishedTime)
                .orderByDesc(ArticlesEntity::getCreateTime)
                .orderByDesc(ArticlesEntity::getId);

        if (StringUtils.hasText(currentRequest.getTag())) {
            queryWrapper.apply("JSON_CONTAINS(tags, JSON_QUOTE({0}))", currentRequest.getTag().trim());
        }
        if (StringUtils.hasText(currentRequest.getKeyword())) {
            String keyword = currentRequest.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like(ArticlesEntity::getTitle, keyword)
                    .or()
                    .like(ArticlesEntity::getContent, keyword));
        }

        Page<ArticlesEntity> page = articlesMapper.selectPage(new Page<>(currentPageNum, currentPageSize), queryWrapper);
        PageResult<ArticlePageVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(currentPageNum);
        pageResult.setPageSize(currentPageSize);
        pageResult.setList(page.getRecords().stream().map(this::buildArticlePageVO).toList());
        return pageResult;
    }

    @Override
    public List<String> listTags() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ArticlesEntity> articles = articlesMapper.selectList(new LambdaQueryWrapper<ArticlesEntity>()
                .select(ArticlesEntity::getTags)
                .eq(ArticlesEntity::getDeleted, 0)
                .eq(ArticlesEntity::getCreateBy, userId)
                .orderByAsc(ArticlesEntity::getCreateTime)
                .orderByAsc(ArticlesEntity::getId));

        LinkedHashSet<String> uniqueTags = new LinkedHashSet<>();
        for (ArticlesEntity article : articles) {
            uniqueTags.addAll(fromJsonTags(article.getTags()));
        }
        return uniqueTags.stream().toList();
    }

    @Override
    public List<String> listPublicTags() {
        List<ArticlesEntity> articles = articlesMapper.selectList(new LambdaQueryWrapper<ArticlesEntity>()
                .select(ArticlesEntity::getTags)
                .eq(ArticlesEntity::getDeleted, 0)
                .eq(ArticlesEntity::getStatus, 1)
                .orderByAsc(ArticlesEntity::getPublishedTime)
                .orderByAsc(ArticlesEntity::getId));

        LinkedHashSet<String> uniqueTags = new LinkedHashSet<>();
        for (ArticlesEntity article : articles) {
            uniqueTags.addAll(fromJsonTags(article.getTags()));
        }
        return uniqueTags.stream().toList();
    }

    @Override
    public String uploadCover(MultipartFile file) {
        return localFileUploadUtil.uploadArticleCover(file);
    }

    private ArticlesEntity requireOwnedArticle(Long articleId, Long userId) {
        ArticlesEntity article = articlesMapper.selectOne(new LambdaQueryWrapper<ArticlesEntity>()
                .eq(ArticlesEntity::getId, articleId)
                .eq(ArticlesEntity::getCreateBy, userId)
                .eq(ArticlesEntity::getDeleted, 0)
                .last("limit 1"));
        BizAssert.notNull(article, ArticleErrorCode.ARTICLE_NOT_FOUND);
        return article;
    }

    private ArticlesEntity requirePublishedArticle(Long articleId) {
        ArticlesEntity article = articlesMapper.selectOne(new LambdaQueryWrapper<ArticlesEntity>()
                .eq(ArticlesEntity::getId, articleId)
                .eq(ArticlesEntity::getDeleted, 0)
                .eq(ArticlesEntity::getStatus, 1)
                .last("limit 1"));
        BizAssert.notNull(article, ArticleErrorCode.ARTICLE_NOT_FOUND);
        return article;
    }

    private void deleteOldCoverIfReplaced(String oldCoverUrl, String newCoverUrl) {
        if (oldCoverUrl == null || oldCoverUrl.isBlank()) {
            return;
        }
        if (Objects.equals(oldCoverUrl, newCoverUrl)) {
            return;
        }
        localFileUploadUtil.deleteByPublicUrl(oldCoverUrl);
    }

    private ArticleDetailVO buildArticleDetailVO(ArticlesEntity article) {
        return ArticleDetailVO.builder()
                .articleId(article.getId())
                .coverUrl(article.getCoverUrl())
                .title(article.getTitle())
                .content(article.getContent())
                .tags(fromJsonTags(article.getTags()))
                .viewNum(article.getViewNum())
                .status(article.getStatus())
                .publishedTime(article.getPublishedTime())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .createBy(article.getCreateBy())
                .updateBy(article.getUpdateBy())
                .build();
    }

    private ArticlePageVO buildArticlePageVO(ArticlesEntity article) {
        return ArticlePageVO.builder()
                .articleId(article.getId())
                .coverUrl(article.getCoverUrl())
                .title(article.getTitle())
                .tags(fromJsonTags(article.getTags()))
                .viewNum(article.getViewNum())
                .status(article.getStatus())
                .content(article.getContent())
                .publishedTime(article.getPublishedTime())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    private void validateArticleRequest(String title, String content, List<String> tags, Integer status) {
        BizAssert.isTrue(StringUtils.hasText(title), ArticleErrorCode.ARTICLE_TITLE_EMPTY);
        BizAssert.isTrue(StringUtils.hasText(content), ArticleErrorCode.ARTICLE_CONTENT_EMPTY);
        validateStatus(status);
        normalizeTags(tags);
    }

    private void validateStatus(Integer status) {
        BizAssert.isTrue(VALID_STATUS.contains(status), ArticleErrorCode.ARTICLE_STATUS_INVALID);
    }

    private List<String> normalizeTags(List<String> tags) {
        BizAssert.notNull(tags, ArticleErrorCode.ARTICLE_TAGS_INVALID);
        try {
            return tags.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .toList();
        } catch (Exception e) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_TAGS_INVALID);
        }
    }

    private LocalDateTime resolvePublishedTime(LocalDateTime currentPublishedTime, Integer status) {
        if (!Integer.valueOf(1).equals(status)) {
            return currentPublishedTime;
        }
        return currentPublishedTime != null ? currentPublishedTime : LocalDateTime.now();
    }

    private String toJsonStorage(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ArticleErrorCode.ARTICLE_TAGS_INVALID);
        }
    }

    private List<String> fromJsonTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(tags, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse article tags", e);
            return List.of();
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
