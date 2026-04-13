package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.domain.dto.article.ArticlePageRequest;
import cn.chenmanman.resume.domain.dto.article.CreateArticleRequestPost;
import cn.chenmanman.resume.domain.dto.article.UpdateArticleRequestPut;
import cn.chenmanman.resume.domain.vo.article.ArticleDetailVO;
import cn.chenmanman.resume.domain.vo.article.ArticlePageVO;
import cn.chenmanman.resume.service.IArticlesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/articles")
public class ArticlesController {

    private final IArticlesService articlesService;

    @PostMapping
    public Result<ArticleDetailVO> publishArticle(@RequestBody @Valid CreateArticleRequestPost request) {
        return Result.success(articlesService.publishArticle(request));
    }

    @PutMapping("/{articleId}")
    public Result<ArticleDetailVO> updateArticle(@PathVariable Long articleId,
                                                 @RequestBody @Valid UpdateArticleRequestPut request) {
        return Result.success(articlesService.updateArticle(articleId, request));
    }

    @DeleteMapping("/{articleId}")
    public Result<Void> deleteArticle(@PathVariable Long articleId) {
        articlesService.deleteArticle(articleId);
        return Result.success();
    }

    @GetMapping("/{articleId}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable Long articleId) {
        return Result.success(articlesService.getArticleDetail(articleId));
    }

    @GetMapping("/public/{articleId}")
    public Result<ArticleDetailVO> getPublicArticleDetail(@PathVariable Long articleId) {
        return Result.success(articlesService.getPublicArticleDetail(articleId));
    }

    @GetMapping("/public/{articleId}/recommend")
    public Result<List<ArticlePageVO>> listPublicRecommendArticles(@PathVariable Long articleId,
                                                                   @RequestParam(required = false) Integer limit) {
        return Result.success(articlesService.listPublicRecommendArticles(articleId, limit));
    }

    @GetMapping("/page")
    public Result<PageResult<ArticlePageVO>> pageArticles(@ModelAttribute ArticlePageRequest request) {
        return Result.success(articlesService.pageArticles(request));
    }

    @GetMapping("/public/page")
    public Result<PageResult<ArticlePageVO>> pagePublicArticles(@ModelAttribute ArticlePageRequest request) {
        return Result.success(articlesService.pagePublicArticles(request));
    }

    @GetMapping("/tags")
    public Result<List<String>> listTags() {
        return Result.success(articlesService.listTags());
    }

    @GetMapping("/public/tags")
    public Result<List<String>> listPublicTags() {
        return Result.success(articlesService.listPublicTags());
    }

    @PostMapping(value = "/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        return Result.success(articlesService.uploadCover(file));
    }
}
