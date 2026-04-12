package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.domain.dto.template.TemplateMatchPageRequest;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.domain.vo.resume.UserFavoriteVO;
import cn.chenmanman.resume.service.ITemplatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/templates")
public class TemplatesController {

    private final ITemplatesService templatesService;

    @GetMapping
    public Result<List<TemplatesVO>> listTemplates() {
        return Result.success(templatesService.listTemplates());
    }

    @GetMapping("/{templateId}")
    public Result<TemplatesVO> getTemplateDetail(@PathVariable Long templateId) {
        return Result.success(templatesService.getTemplateDetail(templateId));
    }

    @GetMapping("/tag-groups")
    public Result<Map<String, List<String>>> getTemplateTagGroups() {
        return Result.success(templatesService.getTemplateTagGroups());
    }

    @GetMapping("/page")
    public Result<PageResult<TemplatesVO>> pageTemplates(@ModelAttribute TemplateMatchPageRequest request) {
        return Result.success(templatesService.pageTemplates(request));
    }


    @GetMapping("/category")
    public Result<List<String>> getTemplateCategory() {
        return Result.success(templatesService.getTemplateCategory());
    }




    @GetMapping("/favorite")
    public Result<String> favoriteTemplate(Long templateId) {
        templatesService.favoriteTemplate(templateId);
        return Result.success();
    }

    @GetMapping("/pageFavorite")
    public Result<PageResult<UserFavoriteVO>> listFavoriteTemplate(PageRequest pageRequest) {

        return Result.success(templatesService.listFavoriteTemplate(pageRequest));
    }

    @GetMapping("/checkFavoriteTemplate")
    public Result<Boolean> isFavoriteTemplate(Long templateId) {
        return Result.success(templatesService.isFavoriteTemplate(templateId));
    }
}
