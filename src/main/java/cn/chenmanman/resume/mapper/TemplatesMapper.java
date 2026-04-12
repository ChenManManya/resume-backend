package cn.chenmanman.resume.mapper;

import cn.chenmanman.resume.domain.entity.resume.TemplatesEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 17383
* @description 针对表【templates(简历模板表)】的数据库操作Mapper
* @createDate 2026-04-02 15:50:17
* @Entity cn.chenmanman.resume.domain.entity.resume.TemplatesEntity
*/
public interface TemplatesMapper extends BaseMapper<TemplatesEntity> {
    List<String> selectTemplateCategory();
}




