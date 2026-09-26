package com.somepro.domain.unit.repository;

import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.unit.model.BloodUnit;
import com.somepro.domain.unit.model.UnitListItem;
import com.somepro.domain.unit.model.UnitQuery;
import reactor.core.publisher.Mono;

/**
 * 血袋仓储端口（领域层定义，基础设施层实现）。
 *
 * 分页返回 {@link UnitListItem}：列表行需要献血者编号/姓名，由仓储 join 档案带出。
 */
public interface BloodUnitRepository {

    Mono<BloodUnit> save(BloodUnit unit);

    Mono<BloodUnit> findById(Long id);

    Mono<PageResult<UnitListItem>> page(UnitQuery query);

    /** 逻辑删除（@TableLogic 置 del_flag=1），删除后不再出现在名单里。 */
    Mono<Void> softDelete(Long id);

    /**
     * 按当前年份取下一个血袋编号（BU-yyyy-序号）。
     * 同献血者取号：含已逻辑删除记录，并发碰撞靠 uk_unit_no 唯一索引兜底 + 重试。
     */
    Mono<String> nextUnitNo();
}
