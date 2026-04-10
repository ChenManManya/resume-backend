package cn.chenmanman.resume.mapper;

import cn.chenmanman.resume.domain.entity.resume.UserTemplateFavoriteEntity;
import cn.chenmanman.resume.domain.vo.resume.UserFavoriteVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface UserTemplateFavoriteMapper extends BaseMapper<UserTemplateFavoriteEntity> {

    Page<UserFavoriteVO> selectUserFavoriteTemplate(Page<?> page, @Param("userId") Long userId);
}
