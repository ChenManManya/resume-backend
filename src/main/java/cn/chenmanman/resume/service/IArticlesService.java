package cn.chenmanman.resume.service;

import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.domain.dto.article.ArticlePageRequest;
import cn.chenmanman.resume.domain.dto.article.CreateArticleRequestPost;
import cn.chenmanman.resume.domain.dto.article.UpdateArticleRequestPut;
import cn.chenmanman.resume.domain.vo.article.ArticleDetailVO;
import cn.chenmanman.resume.domain.vo.article.ArticlePageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticlesService {

    ArticleDetailVO publishArticle(CreateArticleRequestPost request);

    ArticleDetailVO updateArticle(Long articleId, UpdateArticleRequestPut request);

    void deleteArticle(Long articleId);

    ArticleDetailVO getArticleDetail(Long articleId);

    ArticleDetailVO getPublicArticleDetail(Long articleId);

    PageResult<ArticlePageVO> pageArticles(ArticlePageRequest request);

    PageResult<ArticlePageVO> pagePublicArticles(ArticlePageRequest request);

    List<String> listTags();

    List<String> listPublicTags();

    String uploadCover(MultipartFile file);
}
