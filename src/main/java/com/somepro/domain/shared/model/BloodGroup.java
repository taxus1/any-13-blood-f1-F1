package com.somepro.domain.shared.model;

import com.somepro.common.exception.BizException;

/**
 * ABO 血型（领域共享枚举）：A / B / AB / O。
 *
 * 纯领域类型，不带任何框架注解。落库时由仓储适配器转成列上的字符串（t_blood_donor.blood_group、
 * t_blood_unit.blood_group 都是 VARCHAR），对外也只暴露这里的 {@link #name()}。
 */
public enum BloodGroup {
    A,
    B,
    AB,
    O;

    /** 解析外部传入的血型码，大小写不敏感；非法值抛业务异常给出明确取值范围。 */
    public static BloodGroup parse(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("ABO 血型不能为空，取值 A / B / AB / O");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException("ABO 血型非法：" + code + "，取值 A / B / AB / O");
        }
    }
}
