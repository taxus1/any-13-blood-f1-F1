package com.somepro.domain.donor.repository;

import com.somepro.domain.donor.model.Donor;
import com.somepro.domain.donor.model.DonorQuery;
import com.somepro.domain.shared.model.PageResult;
import reactor.core.publisher.Mono;

/**
 * 献血者档案仓储端口（领域层定义，基础设施层实现）。
 * 分页统一返回领域值对象 {@link PageResult}，不依赖任何框架分页类型。
 */
public interface DonorRepository {

    Mono<Donor> save(Donor donor);

    Mono<Donor> findById(Long id);

    Mono<PageResult<Donor>> page(DonorQuery query);

    /** 逻辑删除（@TableLogic 置 del_flag=1），删除后不再出现在名单里。 */
    Mono<Void> softDelete(Long id);

    /**
     * 按当前年份取下一个献血者编号（DNR-yyyy-序号）。
     * 取号需把已逻辑删除的档案也算进去（物理唯一索引 uk_donor_no 不认 del_flag），
     * 由基础设施层走原生 SQL 扫描最大序号，并发碰撞靠唯一索引兜底 + 重试。
     */
    Mono<String> nextDonorNo();

    /**
     * 编号是否已被占用（含已逻辑删除的档案）。
     * 物理唯一索引 uk_donor_no 不认 del_flag：号一旦用过，即使人删了也不能再落到别人头上，
     * 所以查重必须把软删记录算进去，与索引语义保持一致。
     */
    Mono<Boolean> existsByDonorNo(String donorNo);
}
