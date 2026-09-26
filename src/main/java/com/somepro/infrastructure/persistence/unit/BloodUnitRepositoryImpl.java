package com.somepro.infrastructure.persistence.unit;

import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.PageHelper;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.unit.model.BloodUnit;
import com.somepro.domain.unit.model.UnitListItem;
import com.somepro.domain.unit.model.UnitQuery;
import com.somepro.domain.unit.repository.BloodUnitRepository;
import com.somepro.infrastructure.persistence.base.BlockingJdbcSupport;
import com.somepro.infrastructure.persistence.unit.converter.BloodUnitPoConverter;
import com.somepro.infrastructure.persistence.unit.po.BloodUnitPO;
import com.somepro.infrastructure.persistence.unit.po.UnitListRowPO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 血袋仓储适配器（基础设施层）：MyBatis-Plus + 一条手写 join 列表 SQL 实现 {@link BloodUnitRepository}。
 *
 * 约束与献血者仓储一致：Mapper 只在 blocking 桥接里调用、@TableLogic 软删、PageHelper 分页、
 * 编号按年取号（含软删）+ uk_unit_no 兜底，碰撞由应用层重试。
 */
@Repository
public class BloodUnitRepositoryImpl extends BlockingJdbcSupport implements BloodUnitRepository {

    /** BU-年-6 位序号，如 BU-2026-000001。 */
    private static final String NO_FORMAT = "BU-%04d-%06d";

    private final BloodUnitMapper bloodUnitMapper;

    public BloodUnitRepositoryImpl(BloodUnitMapper bloodUnitMapper) {
        this.bloodUnitMapper = bloodUnitMapper;
    }

    @Override
    public Mono<BloodUnit> save(BloodUnit unit) {
        return blocking(() -> {
            BloodUnitPO po = BloodUnitPoConverter.toPo(unit);
            if (po.getId() == null) {
                po.setId(IdUtil.getSnowflakeNextId());
                bloodUnitMapper.insert(po);
            } else {
                bloodUnitMapper.updateById(po);
            }
            return BloodUnitPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<BloodUnit> findById(Long id) {
        return blocking(() -> {
            BloodUnitPO po = bloodUnitMapper.selectById(id);
            return po == null ? null : BloodUnitPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<PageResult<UnitListItem>> page(UnitQuery q) {
        return this.<PageResult<UnitListItem>>blocking(() -> {
            try {
                PageHelper.startPage(q.pageNum(), q.pageSize());
                List<UnitListRowPO> rows = bloodUnitMapper.selectListPage(
                        trimToNull(q.unitNo()),
                        trimToNull(q.donorNo()),
                        trimToNull(q.donorName()),
                        q.bloodGroup() == null ? null : q.bloodGroup().name(),
                        q.rh() == null ? null : q.rh().name(),
                        q.component() == null ? null : q.component().name(),
                        q.status() == null ? null : q.status().name());
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                List<UnitListItem> content = rows.stream()
                        .map(BloodUnitPoConverter::toListItem)
                        .collect(Collectors.toList());
                return new PageResult<>(content, total, q.pageNum(), q.pageSize());
            } finally {
                PageHelper.clearPage();
            }
        });
    }

    @Override
    public Mono<Void> softDelete(Long id) {
        return blocking(() -> {
            bloodUnitMapper.deleteById(id);
            return Boolean.TRUE;
        }).then();
    }

    @Override
    public Mono<String> nextUnitNo() {
        return blocking(() -> {
            int year = LocalDate.now().getYear();
            Integer max = bloodUnitMapper.maxSerialOfYear(year);
            return String.format(NO_FORMAT, year, (max == null ? 0 : max) + 1);
        });
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
