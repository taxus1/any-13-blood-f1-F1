package com.somepro.domain.unit.model;

import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;

import java.time.LocalDateTime;

/**
 * 血袋列表行（领域值对象）。
 *
 * 名单要拿血袋编号跟纸质单对号，同时要能看到是谁献的，所以列表行除血袋自身字段外
 * 冗余带出献血者编号/姓名（仓储用一条 join 查出来，避免逐行回查）。
 */
public record UnitListItem(Long id,
                           String unitNo,
                           Long donorId,
                           String donorNo,
                           String donorName,
                           ComponentType component,
                           BloodGroup bloodGroup,
                           RhFactor rh,
                           int volumeMl,
                           LocalDateTime collectedAt,
                           LocalDateTime expireAt,
                           String storageTemp,
                           UnitStatus status) {
}
