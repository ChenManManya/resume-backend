package cn.chenmanman.resume.controller;

import cn.chenmanman.resume.common.Result;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.service.ITemplatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
