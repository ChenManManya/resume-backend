package cn.chenmanman.resume.domain.dto.resume;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeOptimizeRequest {

    /**
     * 用户原始简历 JSON
     */
    private Map<String, Object> resumeJson;

    /**
     * 操作模式：
     * polish = 润色
     * correct = 纠错
     * expand = 扩写
     * style = 排版优化
     */
    private List<String> modes;
}
