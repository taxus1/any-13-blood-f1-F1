package com.somepro.infrastructure.persistence.unit.po;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 血袋分页列表的查询行（基础设施层）：t_blood_unit LEFT JOIN t_blood_donor 的结果形状。
 *
 * 不是任何一张表的映射（没有 @TableName），只服务于列表查询；列靠全局驼峰映射回填。
 * 用 LEFT JOIN：献血者档案即使被软删，血袋底账也不能跟着从名单里消失，
 * 此时 donorNo / donorName 可能为 null。
 */
@Getter
@Setter
public class UnitListRowPO {

    private Long id;

    private String unitNo;

    private Long donorId;

    private String donorNo;

    private String donorName;

    private String component;

    private String bloodGroup;

    private String rh;

    private Integer volumeMl;

    private LocalDateTime collectedAt;

    private LocalDateTime expireAt;

    private String storageTemp;

    private String status;
}
