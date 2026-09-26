package com.somepro.domain.donor.model;

import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;

/**
 * 献血者分页查询条件（领域值对象）。
 *
 * 所有条件均可为空：一个条件都不填就是整份名单分页。
 * - donorNo：按编号精确对号
 * - name：按姓名模糊
 * - bloodGroup / rh / status：精确匹配
 */
public record DonorQuery(int pageNum,
                         int pageSize,
                         String donorNo,
                         String name,
                         BloodGroup bloodGroup,
                         RhFactor rh,
                         DonorStatus status) {
}
