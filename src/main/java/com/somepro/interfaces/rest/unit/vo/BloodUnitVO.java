package com.somepro.interfaces.rest.unit.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 血袋对外 VO（不可变 record）。
 * 列表行额外带献血者编号/姓名（建档详情里 donorNo/donorName 可能为 null，例如档案已被删）。
 * 不含 delFlag 及审计字段。
 */
public record BloodUnitVO(Long id,
                          String unitNo,
                          Long donorId,
                          String donorNo,
                          String donorName,
                          String component,
                          String bloodGroup,
                          String rh,
                          Integer volumeMl,
                          LocalDateTime collectedAt,
                          LocalDateTime expireAt,
                          String storageTemp,
                          String status,
                          LocalDateTime createTime)
        implements Serializable {
}
