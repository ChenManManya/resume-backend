package cn.chenmanman.resume.service;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.domain.dto.template.TemplateMatchPageRequest;
import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;
import cn.chenmanman.resume.domain.vo.resume.UserFavoriteVO;

import java.util.List;
import java.util.Map;

public interface ITemplatesService {
    List<TemplatesVO> listTemplates();

    TemplatesVO getTemplateDetail(Long templateId);

    List<TemplatesVO> listRecommendTemplates(Long templateId, Integer limit);

    PageResult<TemplatesVO> pageTemplates(TemplateMatchPageRequest pageRequest);

    Map<String, List<String>> getTemplateTagGroups();

    PageResult<UserFavoriteVO> listFavoriteTemplate(PageRequest pageRequest);

    void favoriteTemplate(Long templateId);

    Boolean isFavoriteTemplate(Long templateId);

    List<String> getTemplateCategory();
}
