package com.somepro.domain.unit.model;

import com.somepro.common.exception.BizException;

/**
 * 血液成分（领域枚举）：
 * WHOLE 全血 / RBC 红细胞 / PLASMA 血浆 / PLATELET 血小板。
 */
public enum ComponentType {
    WHOLE,
    RBC,
    PLASMA,
    PLATELET;

    public static ComponentType parse(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("血液成分不能为空，取值 WHOLE 全血 / RBC 红细胞 / PLASMA 血浆 / PLATELET 血小板");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException("血液成分非法：" + code + "，取值 WHOLE / RBC / PLASMA / PLATELET");
        }
    }
}
