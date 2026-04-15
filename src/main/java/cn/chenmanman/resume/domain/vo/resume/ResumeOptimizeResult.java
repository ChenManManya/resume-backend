package cn.chenmanman.resume.domain.vo.resume;

import lombok.Data;

import java.util.Map;

@Data
public class ResumeOptimizeResult {

    /**
     * 优化后的内容 JSON
     */
    private Map<String, Object> content;

    /**
     * 优化后的样式 JSON
     */
    private Map<String, Object> style;
}