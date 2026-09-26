package com.somepro.infrastructure.persistence.donor;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.somepro.domain.donor.model.Donor;
import com.somepro.domain.donor.model.DonorQuery;
import com.somepro.domain.donor.repository.DonorRepository;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.infrastructure.persistence.base.BlockingJdbcSupport;
import com.somepro.infrastructure.persistence.donor.converter.DonorPoConverter;
import com.somepro.infrastructure.persistence.donor.po.DonorPO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 献血者仓储适配器（基础设施层）：MyBatis-Plus 实现 {@link DonorRepository}。
 *
 * - Mapper 只在 {@link BlockingJdbcSupport#blocking} 里调用，绝不在 Netty 线程上跑 JDBC；
 * - 软删除交给 @TableLogic，不手写 del_flag 条件；
 * - 分页统一 PageHelper，用完 PageHelper.clearPage()；
 * - 编号按「当前年 + 库内最大序号 +1」生成（含软删记录），并发碰撞由应用层靠 uk_donor_no 重试。
 */
@Repository
public class DonorRepositoryImpl extends BlockingJdbcSupport implements DonorRepository {

    /** DNR-年-4 位序号，如 DNR-2026-0001。 */
    private static final String NO_FORMAT = "DNR-%04d-%04d";

    private final DonorMapper donorMapper;

    public DonorRepositoryImpl(DonorMapper donorMapper) {
        this.donorMapper = donorMapper;
    }

    @Override
    public Mono<Donor> save(Donor donor) {
        return blocking(() -> {
            DonorPO po = DonorPoConverter.toPo(donor);
            if (po.getId() == null) {
                po.setId(IdUtil.getSnowflakeNextId());
                donorMapper.insert(po);
            } else {
                donorMapper.updateById(po);
            }
            return DonorPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<Donor> findById(Long id) {
        return blocking(() -> {
            DonorPO po = donorMapper.selectById(id);
            return po == null ? null : DonorPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<PageResult<Donor>> page(DonorQuery q) {
        return this.<PageResult<Donor>>blocking(() -> {
            try {
                PageHelper.startPage(q.pageNum(), q.pageSize());
                LambdaQueryWrapper<DonorPO> w = Wrappers.<DonorPO>lambdaQuery()
                        // 翻页每行先带编号，方便跟纸质单对号，再按 id 兜底稳定次序
                        .orderByAsc(DonorPO::getDonorNo)
                        .orderByAsc(DonorPO::getId);
                if (q.donorNo() != null && !q.donorNo().isBlank()) {
                    w.eq(DonorPO::getDonorNo, q.donorNo().trim());
                }
                if (q.name() != null && !q.name().isBlank()) {
                    w.like(DonorPO::getDonorName, q.name().trim());
                }
                if (q.bloodGroup() != null) {
                    w.eq(DonorPO::getBloodGroup, q.bloodGroup().name());
                }
                if (q.rh() != null) {
                    w.eq(DonorPO::getRh, q.rh().name());
                }
                if (q.status() != null) {
                    w.eq(DonorPO::getStatus, q.status().name());
                }
                List<DonorPO> rows = donorMapper.selectList(w);
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                List<Donor> content = rows.stream().map(DonorPoConverter::toDomain).collect(Collectors.toList());
                return new PageResult<>(content, total, q.pageNum(), q.pageSize());
            } finally {
                PageHelper.clearPage();
            }
        });
    }

    @Override
    public Mono<Void> softDelete(Long id) {
        return blocking(() -> {
            donorMapper.deleteById(id);
            return Boolean.TRUE;
        }).then();
    }

    @Override
    public Mono<String> nextDonorNo() {
        return blocking(() -> {
            int year = LocalDate.now().getYear();
            Integer max = donorMapper.maxSerialOfYear(year);
            return String.format(NO_FORMAT, year, (max == null ? 0 : max) + 1);
        });
    }

    @Override
    public Mono<Boolean> existsByDonorNo(String donorNo) {
        return blocking(() -> donorMapper.countByDonorNoIncludeDeleted(donorNo) > 0);
    }
}
