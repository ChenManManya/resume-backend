package cn.chenmanman.resume.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应
 * */
@Data
public class PageResult<T> {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> list;
}