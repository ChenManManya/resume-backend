package cn.chenmanman.resume.service;

import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.domain.dto.template.TemplateMatchPageRequest;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;

import java.util.List;
import java.util.Map;

public interface ITemplatesService {
    List<TemplatesVO> listTemplates();

    TemplatesVO getTemplateDetail(Long templateId);

    PageResult<TemplatesVO> pageTemplates(TemplateMatchPageRequest pageRequest);

    Map<String, List<String>> getTemplateTagGroups();
}
