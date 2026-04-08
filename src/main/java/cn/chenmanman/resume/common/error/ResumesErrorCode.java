package cn.chenmanman.resume.common.error;

import lombok.Getter;

@Getter
public enum ResumesErrorCode implements ErrorCode {

    TEMPLATE_NOT_FOUND(600, "简历模板不存在!"),
    TEMPLATE_DISABLED(601, "简历模板已停用!"),
    RESUME_NOT_FOUND(602, "简历不存在!"),
    RESUME_VERSION_NOT_FOUND(603, "简历版本不存在!"),
    RESUME_CURRENT_VERSION_NOT_FOUND(604, "简历当前版本不存在!"),
    RESUME_EXPORT_NOT_SUPPORTED(605, "当前项目未集成简历导出渲染能力!");

    private final Integer code;
    private final String message;

    ResumesErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
