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
 *
 * 编号唯一性两条路：
 * - 登记：录入员指定了编号就先查重、撞号直接挡（不换号）；没指定由系统按年取号，
 *   取号与插入是两次独立调用，并发下可能拿到同一个号 —— 由 uk_donor_no 唯一索引兜底，
 *   撞号时用同一份命令重建聚合重新取号重试（编号只允许分配一次，所以重试必须重建聚合）。
 * - 修改：改成别人正在用的号要在落库前挡回（先查重再保存，挡回时一次写库都没发起，
 *   原档案保持原样）；改成没人用的新号才落库。查重与保存之间仍有并发窗口，
 *   由 uk_donor_no 兜底转成业务异常。
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
            return createWithSpecifiedNo(cmd);
        }
        return assignNoAndSave(cmd, 1);
    }

    /** 录入员指定编号：撞号直接挡回（不像系统取号那样换号重试），并发撞库由唯一索引兜底。 */
    private Mono<Donor> createWithSpecifiedNo(CreateDonorCmd cmd) {
        String no = cmd.donorNo().trim();
        return donorRepository.existsByDonorNo(no)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BizException("献血者编号已被占用：" + no));
                    }
                    Donor donor = Donor.register(cmd.name(), cmd.gender(), cmd.bloodGroup(), cmd.rh(), cmd.phone());
                    donor.assignDonorNo(no);
                    return donorRepository.save(donor);
                })
                .onErrorMap(DuplicateKeyException.class,
                        e -> new BizException("献血者编号已被占用：" + no));
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
                    donor.updateProfile(cmd.name(), cmd.gender(), cmd.bloodGroup(), cmd.rh(), cmd.phone());
                    if (cmd.status() != null && !cmd.status().isBlank()) {
                        donor.changeStatus(DonorStatus.parse(cmd.status()));
                    }
                    String newNo = cmd.donorNo() == null ? null : cmd.donorNo().trim();
                    if (newNo == null || newNo.isEmpty() || newNo.equals(donor.getDonorNo())) {
                        return donorRepository.save(donor);
                    }
                    // 改号：先查重再落库。撞号直接挡回 —— 此时未发起任何写库，原档案保持原样
                    return donorRepository.existsByDonorNo(newNo)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new BizException(
                                            "献血者编号已被占用：" + newNo + "，修改未生效"));
                                }
                                donor.changeDonorNo(newNo);
                                return donorRepository.save(donor);
                            });
                })
                .onErrorMap(DuplicateKeyException.class,
                        e -> new BizException("献血者编号已被占用，修改未生效，请重试"));
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
                // 用 flatMap 而不是 then(repo.softDelete(id))：后者会立即调用仓储方法，
                // 档案不存在时也会白调一次（且 then 的参数不允许为 null）
                .flatMap(donor -> donorRepository.softDelete(id));
    }
}
