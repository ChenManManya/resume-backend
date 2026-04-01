package cn.chenmanman.resume.common.exception;

import cn.chenmanman.resume.common.Result;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===================== 1. @Valid 参数校验异常 =====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {

        String msg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return Result.fail(400, msg);
    }

    // ===================== 2. 单个参数校验异常（@Validated） =====================

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintException(ConstraintViolationException e) {

        String msg = e.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ":" + v.getMessage())
                .collect(Collectors.joining(", "));

        return Result.fail(400, msg);
    }


    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ===================== 4. Sa-Token 未登录 =====================

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        return Result.fail(401, "未登录或登录已过期");
    }

    // ===================== 5. Sa-Token 无权限 =====================

    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        return Result.fail(403, "无权限访问");
    }

    // ===================== 6. 兜底异常 =====================

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace(); // 建议换成日志
        return Result.fail(500, "系统异常");
    }
}