package cn.chenmanman.resume.common.error;

import lombok.Getter;

@Getter
public enum CommonErrorCode implements ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),

    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统异常"),

    BUSINESS_ERROR(1000, "业务异常"),
    PARAM_ERROR(1001, "参数校验失败"),
    DATA_NOT_EXIST(1002, "数据不存在"),
    DATA_ALREADY_EXIST(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败");

    private final Integer code;
    private final String message;

    CommonErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}