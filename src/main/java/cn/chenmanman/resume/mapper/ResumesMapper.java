package cn.chenmanman.resume.mapper;

import cn.chenmanman.resume.domain.entity.resume.ResumesEntity;
import cn.chenmanman.resume.domain.vo.resume.MyResumesVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 17383
* @description 针对表【resumes(简历主表)】的数据库操作Mapper
* @createDate 2026-04-02 15:50:17
* @Entity cn.chenmanman.resume.domain.entity.resume.ResumesEntity
*/
public interface ResumesMapper extends BaseMapper<ResumesEntity> {

    List<MyResumesVO> getResumeMeList(@Param("userId") Long userId);

    Page<MyResumesVO> getResumeMePage(Page<?> page, @Param("userId") Long userId);
}




