package com.somepro.domain.donor.model;

/** 献血者性别（领域枚举）：MALE 男 / FEMALE 女。 */
public enum Gender {
    MALE,
    FEMALE;

    public static Gender parse(String code) {
        if (code == null || code.isBlank()) {
            throw new com.somepro.common.exception.BizException("性别不能为空，取值 MALE 男 / FEMALE 女");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new com.somepro.common.exception.BizException("性别非法：" + code + "，取值 MALE / FEMALE");
        }
    }
}
