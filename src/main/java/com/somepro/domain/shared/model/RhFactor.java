package com.somepro.domain.shared.model;

import com.somepro.common.exception.BizException;

/**
 * Rh 因子（领域共享枚举）：POSITIVE 阳性 / NEGATIVE 阴性。
 *
 * 献血者档案与血袋都用同一套取值：血袋不另填，建档时从献血者档案原样带出。
 */
public enum RhFactor {
    POSITIVE,
    NEGATIVE;

    /** 解析外部传入的 Rh 码，大小写不敏感；非法值抛业务异常给出明确取值范围。 */
    public static RhFactor parse(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("Rh 因子不能为空，取值 POSITIVE 阳性 / NEGATIVE 阴性");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException("Rh 因子非法：" + code + "，取值 POSITIVE / NEGATIVE");
        }
    }
}
