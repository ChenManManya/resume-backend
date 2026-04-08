package cn.chenmanman.resume.service;

import cn.chenmanman.resume.domain.vo.resume.TemplatesVO;

import java.util.List;

public interface ITemplatesService {
    List<TemplatesVO> listTemplates();

    TemplatesVO getTemplateDetail(Long templateId);
}
