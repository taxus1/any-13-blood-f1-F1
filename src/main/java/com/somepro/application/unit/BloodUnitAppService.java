package com.somepro.application.unit;

import com.somepro.application.unit.cmd.CreateUnitCmd;
import com.somepro.application.unit.cmd.UpdateUnitCmd;
import com.somepro.common.exception.BizException;
import com.somepro.domain.donor.model.Donor;
import com.somepro.domain.donor.repository.DonorRepository;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.unit.model.BloodUnit;
import com.somepro.domain.unit.model.UnitListItem;
import com.somepro.domain.unit.model.UnitQuery;
import com.somepro.domain.unit.model.UnitStatus;
import com.somepro.domain.unit.repository.BloodUnitRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 血袋应用服务：编排登记、修改、查看、删除、分页查询用例。
 *
 * 登记时不接受调用方传血型 —— 按 donorId 读献血者档案，档案上怎么写，这袋血就照什么样带出。
 * 编号取号、撞号重试的策略与献血者一致（uk_unit_no 唯一索引兜底）。
 */
@Service
public class BloodUnitAppService {

    private static final int MAX_ATTEMPTS = 3;

    private final BloodUnitRepository bloodUnitRepository;
    private final DonorRepository donorRepository;

    public BloodUnitAppService(BloodUnitRepository bloodUnitRepository, DonorRepository donorRepository) {
        this.bloodUnitRepository = bloodUnitRepository;
        this.donorRepository = donorRepository;
    }

    public Mono<BloodUnit> create(CreateUnitCmd cmd) {
        // 先读献血者：必须存在；血型从档案带出，不由调用方填
        return donorRepository.findById(cmd.donorId())
                .switchIfEmpty(Mono.error(new BizException("献血者不存在或已删除，无法登记血袋：donorId=" + cmd.donorId())))
                .flatMap(donor -> assignNoAndSave(cmd, donor, 1));
    }

    private Mono<BloodUnit> assignNoAndSave(CreateUnitCmd cmd, Donor donor, int attempt) {
        return bloodUnitRepository.nextUnitNo()
                .flatMap(no -> {
                    // 每次尝试重建聚合，理由同献血者取号重试
                    BloodUnit unit = BloodUnit.collect(donor.getId(), donor.getBloodGroup(), donor.getRh(),
                            cmd.component(), cmd.volumeMl(), cmd.collectedAt(), cmd.expireAt(), cmd.storageTemp());
                    unit.assignUnitNo(no);
                    return bloodUnitRepository.save(unit);
                })
                .onErrorResume(DuplicateKeyException.class, e -> {
                    if (attempt >= MAX_ATTEMPTS) {
                        return Mono.error(new BizException("血袋编号冲突，多次取号仍失败，请重试"));
                    }
                    return assignNoAndSave(cmd, donor, attempt + 1);
                });
    }

    public Mono<BloodUnit> update(Long id, UpdateUnitCmd cmd) {
        return bloodUnitRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("血袋不存在或已删除：id=" + id)))
                .flatMap(unit -> {
                    unit.updateInfo(cmd.component(), cmd.volumeMl(),
                            cmd.collectedAt(), cmd.expireAt(), cmd.storageTemp());
                    if (cmd.status() != null && !cmd.status().isBlank()) {
                        unit.changeStatus(UnitStatus.parse(cmd.status()));
                    }
                    return bloodUnitRepository.save(unit);
                });
    }

    public Mono<BloodUnit> detail(Long id) {
        return bloodUnitRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("血袋不存在或已删除：id=" + id)));
    }

    public Mono<PageResult<UnitListItem>> page(UnitQuery query) {
        return bloodUnitRepository.page(query);
    }

    public Mono<Void> delete(Long id) {
        return bloodUnitRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("血袋不存在或已删除：id=" + id)))
                .then(bloodUnitRepository.softDelete(id));
    }
}
