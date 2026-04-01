package cn.chenmanman.resume.common.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum UserStatusEnum {
    DISABLED(0),
    ENABLED(1);

    private final Integer value;

    UserStatusEnum(int i) {
        this.value = i;
    }
}