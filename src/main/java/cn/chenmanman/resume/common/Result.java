package cn.chenmanman.resume.common;

import cn.chenmanman.resume.common.error.CommonErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 时间戳
     */
    private Long timestamp;

    // ===================== 成功 =====================

    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(CommonErrorCode.SUCCESS.getCode())
                .message(CommonErrorCode.SUCCESS.getMessage())
                .success(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(CommonErrorCode.SUCCESS.getCode())
                .message(CommonErrorCode.SUCCESS.getMessage())
                .data(data)
                .success(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(CommonErrorCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .success(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ===================== 失败 =====================

    public static <T> Result<T> fail() {
        return Result.<T>builder()
                .code(CommonErrorCode.SYSTEM_ERROR.getCode())
                .message(CommonErrorCode.SYSTEM_ERROR.getMessage())
                .success(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> fail(String message) {
        return Result.<T>builder()
                .code(CommonErrorCode.SYSTEM_ERROR.getCode())
                .message(message)
                .success(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .success(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}