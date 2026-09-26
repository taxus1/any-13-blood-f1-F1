package com.somepro.application.donor;

import com.somepro.application.donor.cmd.CreateDonorCmd;
import com.somepro.application.donor.cmd.UpdateDonorCmd;
import com.somepro.common.exception.BizException;
import com.somepro.domain.donor.model.Donor;
import com.somepro.domain.donor.model.Gender;
import com.somepro.domain.donor.repository.DonorRepository;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 献血者应用服务的编号唯一性编排（mock 仓储，不碰数据库）：
 * 登记撞号要挡、改号撞号要挡且原档案不动、改成新号才落库、系统取号撞号会换号重试。
 */
class DonorAppServiceTest {

    private final DonorRepository repository = mock(DonorRepository.class);
    private final DonorAppService service = new DonorAppService(repository);

    private static CreateDonorCmd createCmd(String donorNo) {
        return new CreateDonorCmd("张三", Gender.MALE, BloodGroup.O, RhFactor.POSITIVE, "13800000000", donorNo);
    }

    private static UpdateDonorCmd updateCmd(String donorNo) {
        return new UpdateDonorCmd("张三", Gender.MALE, BloodGroup.O, RhFactor.POSITIVE, "13800000000", null, donorNo);
    }

    private static Donor persistedDonor(Long id, String donorNo) {
        Donor d = Donor.register("张三", Gender.MALE, BloodGroup.O, RhFactor.POSITIVE, "13800000000");
        d.assignDonorNo(donorNo);
        d.setId(id);
        return d;
    }

    // ---------- 登记 ----------

    @Test
    void createWithSpecifiedNoSavesWhenFree() {
        when(repository.existsByDonorNo("DNR-2026-0001")).thenReturn(Mono.just(false));
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(createCmd("DNR-2026-0001")))
                .assertNext(d -> assertEquals("DNR-2026-0001", d.getDonorNo()))
                .verifyComplete();
        verify(repository, never()).nextDonorNo();
    }

    @Test
    void createWithSpecifiedNoBlocksWhenTaken() {
        when(repository.existsByDonorNo("DNR-2026-0001")).thenReturn(Mono.just(true));

        StepVerifier.create(service.create(createCmd("DNR-2026-0001")))
                .expectError(BizException.class)
                .verify();
        verify(repository, never()).save(any());
        verify(repository, never()).nextDonorNo();
    }

    @Test
    void createWithSpecifiedNoBlocksOnConcurrentDuplicateKey() {
        when(repository.existsByDonorNo("DNR-2026-0001")).thenReturn(Mono.just(false));
        when(repository.save(any())).thenReturn(Mono.error(new DuplicateKeyException("uk_donor_no")));

        StepVerifier.create(service.create(createCmd("DNR-2026-0001")))
                .expectError(BizException.class)
                .verify();
    }

    @Test
    void createWithoutNoUsesSystemAssignedNumber() {
        when(repository.nextDonorNo()).thenReturn(Mono.just("DNR-2026-0007"));
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(createCmd(null)))
                .assertNext(d -> assertEquals("DNR-2026-0007", d.getDonorNo()))
                .verifyComplete();
        verify(repository, never()).existsByDonorNo(anyString());
    }

    @Test
    void createWithoutNoRetriesOnDuplicateKey() {
        when(repository.nextDonorNo())
                .thenReturn(Mono.just("DNR-2026-0001"))
                .thenReturn(Mono.just("DNR-2026-0002"));
        when(repository.save(any()))
                .thenReturn(Mono.error(new DuplicateKeyException("uk_donor_no")))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(createCmd(null)))
                .assertNext(d -> assertEquals("DNR-2026-0002", d.getDonorNo()))
                .verifyComplete();
        verify(repository, times(2)).nextDonorNo();
    }

    // ---------- 修改 ----------

    @Test
    void updateToNumberUsedByAnotherIsBlockedAndOriginalUntouched() {
        Donor existing = persistedDonor(1L, "DNR-2026-0001");
        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.existsByDonorNo("DNR-2026-0002")).thenReturn(Mono.just(true));

        StepVerifier.create(service.update(1L, updateCmd("DNR-2026-0002")))
                .expectError(BizException.class)
                .verify();
        // 一次写库都没发起，原档案保持原样
        verify(repository, never()).save(any());
        assertEquals("DNR-2026-0001", existing.getDonorNo());
    }

    @Test
    void updateToFreshNumberSaves() {
        Donor existing = persistedDonor(1L, "DNR-2026-0001");
        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.existsByDonorNo("DNR-2026-0009")).thenReturn(Mono.just(false));
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.update(1L, updateCmd("DNR-2026-0009")))
                .assertNext(d -> assertEquals("DNR-2026-0009", d.getDonorNo()))
                .verifyComplete();
    }

    @Test
    void updateWithoutNumberKeepsOriginal() {
        Donor existing = persistedDonor(1L, "DNR-2026-0001");
        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.update(1L, updateCmd(null)))
                .assertNext(d -> assertEquals("DNR-2026-0001", d.getDonorNo()))
                .verifyComplete();
        verify(repository, never()).existsByDonorNo(anyString());
    }

    @Test
    void updateWithSameNumberSkipsUniquenessCheck() {
        Donor existing = persistedDonor(1L, "DNR-2026-0001");
        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.update(1L, updateCmd("DNR-2026-0001")))
                .assertNext(d -> assertEquals("DNR-2026-0001", d.getDonorNo()))
                .verifyComplete();
        verify(repository, never()).existsByDonorNo(anyString());
    }

    @Test
    void updateMissingDonorFails() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.update(99L, updateCmd(null)))
                .expectError(BizException.class)
                .verify();
        verify(repository, never()).save(any());
    }

    // ---------- 删除 ----------

    @Test
    void deleteExistingSoftDeletes() {
        when(repository.findById(1L)).thenReturn(Mono.just(persistedDonor(1L, "DNR-2026-0001")));
        when(repository.softDelete(1L)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(1L)).verifyComplete();
        verify(repository).softDelete(1L);
    }

    @Test
    void deleteMissingFailsWithoutTouchingRepository() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(99L))
                .expectError(BizException.class)
                .verify();
        verify(repository, never()).softDelete(any());
    }
}
