package com.somepro.domain.unit.model;

import com.somepro.common.exception.BizException;

/**
 * 血袋状态（领域枚举）：
 * COLLECTED 已采集待检（新登记默认）/ QUALIFIED 检测合格 / REJECTED 检测不合格 /
 * IN_STOCK 在库 / ISSUED 已发放 / DISCARDED 已报废。
 *
 * 后续检测、在库、发放、报废用例各自驱动相应的状态迁移，本阶段只提供状态读取与基础迁移。
 */
public enum UnitStatus {
    COLLECTED,
    QUALIFIED,
    REJECTED,
    IN_STOCK,
    ISSUED,
    DISCARDED;

    public static UnitStatus parse(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("血袋状态不能为空，取值 COLLECTED / QUALIFIED / REJECTED / IN_STOCK / ISSUED / DISCARDED");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException("血袋状态非法：" + code
                    + "，取值 COLLECTED / QUALIFIED / REJECTED / IN_STOCK / ISSUED / DISCARDED");
        }
    }
}
