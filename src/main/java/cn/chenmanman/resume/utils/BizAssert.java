package cn.chenmanman.resume.utils;

import cn.chenmanman.resume.common.error.ErrorCode;
import cn.chenmanman.resume.common.exception.BusinessException;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;


/**
 * 断言工具
 * */
public final class BizAssert {

    private BizAssert() {
    }

    // ===================== 基础断言 =====================

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    public static void isTrue(boolean expression, ErrorCode errorCode) {
        if (!expression) {
            throw new BusinessException(errorCode);
        }
    }

    public static void isTrue(boolean expression, Integer code, String message) {
        if (!expression) {
            throw new BusinessException(code, message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new BusinessException(message);
        }
    }

    public static void isFalse(boolean expression, ErrorCode errorCode) {
        if (expression) {
            throw new BusinessException(errorCode);
        }
    }

    // ===================== 空值断言 =====================

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(message);
        }
    }

    public static void notNull(Object obj, ErrorCode errorCode) {
        if (obj == null) {
            throw new BusinessException(errorCode);
        }
    }

    public static void isNull(Object obj, String message) {
        if (obj != null) {
            throw new BusinessException(message);
        }
    }

    public static void isNull(Object obj, ErrorCode errorCode) {
        if (obj != null) {
            throw new BusinessException(errorCode);
        }
    }

    // ===================== 字符串断言 =====================

    public static void hasText(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    public static void hasText(String text, ErrorCode errorCode) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(errorCode);
        }
    }

    public static void notBlank(String text, String message) {
        hasText(text, message);
    }

    public static void notBlank(String text, ErrorCode errorCode) {
        hasText(text, errorCode);
    }

    public static void isBlank(String text, String message) {
        if (text != null && !text.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    // ===================== 集合断言 =====================

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, ErrorCode errorCode) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(errorCode);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, ErrorCode errorCode) {
        if (map == null || map.isEmpty()) {
            throw new BusinessException(errorCode);
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Object[] array, ErrorCode errorCode) {
        if (array == null || array.length == 0) {
            throw new BusinessException(errorCode);
        }
    }

    // ===================== 对象比较断言 =====================

    public static void equals(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throw new BusinessException(message);
        }
    }

    public static void equals(Object obj1, Object obj2, ErrorCode errorCode) {
        if (!Objects.equals(obj1, obj2)) {
            throw new BusinessException(errorCode);
        }
    }

    public static void notEquals(Object obj1, Object obj2, String message) {
        if (Objects.equals(obj1, obj2)) {
            throw new BusinessException(message);
        }
    }

    public static void notEquals(Object obj1, Object obj2, ErrorCode errorCode) {
        if (Objects.equals(obj1, obj2)) {
            throw new BusinessException(errorCode);
        }
    }

    // ===================== 范围断言 =====================

    public static void greaterThanZero(Number number, String message) {
        if (number == null || number.doubleValue() <= 0) {
            throw new BusinessException(message);
        }
    }

    public static void greaterThanZero(Number number, ErrorCode errorCode) {
        if (number == null || number.doubleValue() <= 0) {
            throw new BusinessException(errorCode);
        }
    }

    public static void greaterOrEqualZero(Number number, String message) {
        if (number == null || number.doubleValue() < 0) {
            throw new BusinessException(message);
        }
    }

    public static void between(int value, int min, int max, String message) {
        if (value < min || value > max) {
            throw new BusinessException(message);
        }
    }

    public static void between(long value, long min, long max, String message) {
        if (value < min || value > max) {
            throw new BusinessException(message);
        }
    }

    // ===================== 直接抛异常 =====================

    public static void fail(String message) {
        throw new BusinessException(message);
    }

    public static void fail(Integer code, String message) {
        throw new BusinessException(code, message);
    }

    public static void fail(ErrorCode errorCode) {
        throw new BusinessException(errorCode);
    }
}