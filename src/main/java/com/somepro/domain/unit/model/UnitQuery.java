package com.somepro.domain.unit.model;

import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;

/**
 * 血袋分页查询条件（领域值对象）。
 *
 * 所有条件均可为空：一个条件都不填就是整份名单分页，可任意组合。
 * - unitNo：按血袋编号精确对号
 * - donorNo / donorName：献血者编号精确、姓名模糊（跨到献血者档案）
 * - bloodGroup / rh / component / status：精确匹配
 */
public record UnitQuery(int pageNum,
                        int pageSize,
                        String unitNo,
                        String donorNo,
                        String donorName,
                        BloodGroup bloodGroup,
                        RhFactor rh,
                        ComponentType component,
                        UnitStatus status) {
}
