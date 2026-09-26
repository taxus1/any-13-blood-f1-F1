package com.somepro.application.donor;

import com.somepro.application.donor.cmd.CreateDonorCmd;
import com.somepro.application.donor.cmd.UpdateDonorCmd;
import com.somepro.common.exception.BizException;
import com.somepro.domain.donor.model.Donor;
import com.somepro.domain.donor.model.DonorQuery;
import com.somepro.domain.donor.model.DonorStatus;
import com.somepro.domain.donor.repository.DonorRepository;
import com.somepro.domain.shared.model.PageResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 献血者档案应用服务：编排登记、修改、查看、删除、分页查询用例。
 *
 * 出入参都是领域对象 / 应用命令，不认识 PO、VO。
 * 编号防撞两条链都走「先领域校验 → 再查库挡重 → 最后落库」，任何一步不过都不写库：
 * - 登记：调用方指定编号则撞号即拒（不重试，号是人选的）；未指定则由系统按年取号，
 *   取号与插入是两次独立调用，并发撞号由 uk_donor_no 唯一索引兜底，用同一份命令重建
 *   聚合重新取号重试（编号只允许分配一次，所以重试必须重建聚合，不能在原对象上改号）。
 * - 修改：换号前查重，撞上别人（含已删除档案）在用的号 → 整次修改拒绝落库，原档案保持
 *   不动；并发下两人同时换同一个新号，由 uk_donor_no 兜底，后到的收到明确失败。
 */
@Service
public class DonorAppService {

    /** 取号-插入的最大尝试次数：正常一次成功，撞号最多再取两次。 */
    private static final int MAX_ATTEMPTS = 3;

    private final DonorRepository donorRepository;

    public DonorAppService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public Mono<Donor> create(CreateDonorCmd cmd) {
        if (cmd.donorNo() != null && !cmd.donorNo().isBlank()) {
            return createWithGivenNo(cmd);
        }
        return assignNoAndSave(cmd, 1);
    }

    /** 调用方指定编号登记：先过领域校验（格式），再查重挡撞号；并发兜底靠 uk_donor_no，不换号重试。 */
    private Mono<Donor> createWithGivenNo(CreateDonorCmd cmd) {
        Donor donor = Donor.register(cmd.name(), cmd.gender(), cmd.bloodGroup(), cmd.rh(), cmd.phone());
        donor.assignDonorNo(cmd.donorNo());
        String no = donor.getDonorNo();
        return donorRepository.existsByDonorNo(no)
                .flatMap(exists -> exists
                        ? Mono.error(new BizException("献血者编号已被占用：" + no))
                        : donorRepository.save(donor))
                .onErrorResume(DuplicateKeyException.class,
                        e -> Mono.error(new BizException("献血者编号已被占用：" + no + "，请换一个")));
    }

    private Mono<Donor> assignNoAndSave(CreateDonorCmd cmd, int attempt) {
        return donorRepository.nextDonorNo()
                .flatMap(no -> {
                    // 每次尝试都从命令重建聚合：编号一经分配不可更改，撞号重试时旧对象不能复用
                    Donor donor = Donor.register(cmd.name(), cmd.gender(), cmd.bloodGroup(), cmd.rh(), cmd.phone());
                    donor.assignDonorNo(no);
                    return donorRepository.save(donor);
                })
                .onErrorResume(DuplicateKeyException.class, e -> {
                    if (attempt >= MAX_ATTEMPTS) {
                        return Mono.error(new BizException("献血者编号冲突，多次取号仍失败，请重试"));
                    }
                    return assignNoAndSave(cmd, attempt + 1);
                });
    }

    public Mono<Donor> update(Long id, UpdateDonorCmd cmd) {
        return donorRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("献血者不存在或已删除：id=" + id)))
                .flatMap(donor -> {
                    // 先过领域校验：任何一项非法都在落库前抛出，原档案保持不动
                    donor.updateProfile(cmd.name(), cmd.gender(), cmd.bloodGroup(), cmd.rh(), cmd.phone());
                    if (cmd.status() != null && !cmd.status().isBlank()) {
                        donor.changeStatus(DonorStatus.parse(cmd.status()));
                    }
                    String newNo = cmd.donorNo() == null ? null : cmd.donorNo().trim();
                    if (newNo == null || newNo.isEmpty() || newNo.equals(donor.getDonorNo())) {
                        return donorRepository.save(donor);
                    }
                    // 换号：先校验格式（只动内存对象），再查库挡撞号；撞号则整次修改不落库
                    donor.changeDonorNo(newNo);
                    return donorRepository.existsByDonorNo(newNo)
                            .flatMap(exists -> exists
                                    ? Mono.error(new BizException("献血者编号已被占用：" + newNo + "，修改未生效"))
                                    : donorRepository.save(donor));
                })
                // 并发换同一个新号：唯一索引兜底，后到者收到明确失败而不是 500
                .onErrorResume(DuplicateKeyException.class,
                        e -> Mono.error(new BizException("献血者编号已被占用，修改未生效，请换一个编号")));
    }

    public Mono<Donor> detail(Long id) {
        return donorRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("献血者不存在或已删除：id=" + id)));
    }

    public Mono<PageResult<Donor>> page(DonorQuery query) {
        return donorRepository.page(query);
    }

    public Mono<Void> delete(Long id) {
        return donorRepository.findById(id)
                .switchIfEmpty(Mono.error(new BizException("献血者不存在或已删除：id=" + id)))
                .then(donorRepository.softDelete(id));
    }
}
