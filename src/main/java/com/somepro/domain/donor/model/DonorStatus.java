package com.somepro.domain.donor.model;

import com.somepro.common.exception.BizException;

/**
 * 献血者状态（领域枚举）。
 * ACTIVE 可献 / DEFERRED 暂缓（暂时不合适）/ BLOCKED 屏蔽（拉黑）。
 * 新建献血者默认 {@link #ACTIVE}。
 */
public enum DonorStatus {
    ACTIVE,
    DEFERRED,
    BLOCKED;

    public static DonorStatus parse(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("献血者状态不能为空，取值 ACTIVE / DEFERRED / BLOCKED");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException("献血者状态非法：" + code + "，取值 ACTIVE / DEFERRED / BLOCKED");
        }
    }
}
