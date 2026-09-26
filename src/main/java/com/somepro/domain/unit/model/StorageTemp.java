package com.somepro.domain.unit.model;

import com.somepro.common.exception.BizException;

/**
 * 保存温度档（领域枚举）：
 * COLD 冷藏档 2-6 / ROOM_TEMP 室温档 20-24。
 *
 * 对外、落库统一用档位编码 {@code 2-6} / {@code 20-24}（列 storage_temp 是 VARCHAR，直接存编码串），
 * 不使用枚举名，避免把 ROOM_TEMP 这种内部命名暴露到接口和单据上。
 */
public enum StorageTemp {

    COLD("2-6"),
    ROOM_TEMP("20-24");

    private final String code;

    StorageTemp(String code) {
        this.code = code;
    }

    /** 档位编码（2-6 / 20-24），落库与对外都用它。 */
    public String code() {
        return code;
    }

    public static StorageTemp parse(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("保存温度档不能为空，取值 2-6 冷藏 / 20-24 室温");
        }
        String normalized = code.trim();
        for (StorageTemp t : values()) {
            if (t.code.equals(normalized)) {
                return t;
            }
        }
        throw new BizException("保存温度档非法：" + code + "，取值 2-6 / 20-24");
    }
}
