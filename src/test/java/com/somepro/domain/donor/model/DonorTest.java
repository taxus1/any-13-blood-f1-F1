package com.somepro.domain.donor.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 献血者聚合根的纯领域行为：登记默认值、编号分配/更正、资料校验、献血回写。 */
class DonorTest {

    private Donor newDonor() {
        return Donor.register("张三", Gender.MALE, BloodGroup.O, RhFactor.POSITIVE, "13800000000");
    }

    @Test
    void registerDefaultsToActiveWithZeroTotal() {
        Donor d = newDonor();
        assertEquals(DonorStatus.ACTIVE, d.getStatus());
        assertEquals(0, d.getTotalMl());
        assertNull(d.getLastDonateAt());
        assertNull(d.getDonorNo());
    }

    @Test
    void assignDonorNoAcceptsWellFormedNumber() {
        Donor d = newDonor();
        d.assignDonorNo("DNR-2026-0001");
        assertEquals("DNR-2026-0001", d.getDonorNo());
    }

    @Test
    void assignDonorNoRejectsBadFormat() {
        Donor d = newDonor();
        assertThrows(BizException.class, () -> d.assignDonorNo("2026-0001"));
        assertThrows(BizException.class, () -> d.assignDonorNo("DNR-2026"));
        assertThrows(BizException.class, () -> d.assignDonorNo(""));
        assertThrows(BizException.class, () -> d.assignDonorNo(null));
    }

    @Test
    void assignDonorNoIsOneTimeOnly() {
        Donor d = newDonor();
        d.assignDonorNo("DNR-2026-0001");
        assertThrows(BizException.class, () -> d.assignDonorNo("DNR-2026-0002"));
        assertEquals("DNR-2026-0001", d.getDonorNo());
    }

    @Test
    void changeDonorNoAllowsCorrectionToNewNumber() {
        Donor d = newDonor();
        d.assignDonorNo("DNR-2026-0001");
        d.changeDonorNo("DNR-2026-0002");
        assertEquals("DNR-2026-0002", d.getDonorNo());
    }

    @Test
    void changeDonorNoStillValidatesFormat() {
        Donor d = newDonor();
        d.assignDonorNo("DNR-2026-0001");
        assertThrows(BizException.class, () -> d.changeDonorNo("随便一个号"));
        assertEquals("DNR-2026-0001", d.getDonorNo());
    }

    @Test
    void updateProfileRejectsMissingRequiredFields() {
        Donor d = newDonor();
        assertThrows(BizException.class, () -> d.updateProfile(" ", Gender.MALE, BloodGroup.O, RhFactor.POSITIVE, null));
        assertThrows(BizException.class, () -> d.updateProfile("张三", null, BloodGroup.O, RhFactor.POSITIVE, null));
        assertThrows(BizException.class, () -> d.updateProfile("张三", Gender.MALE, null, RhFactor.POSITIVE, null));
        assertThrows(BizException.class, () -> d.updateProfile("张三", Gender.MALE, BloodGroup.O, null, null));
    }

    @Test
    void recordDonationAccumulatesAndRefreshesLastTime() {
        Donor d = newDonor();
        LocalDateTime t1 = LocalDateTime.of(2026, 1, 10, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 6, 10, 9, 0);
        d.recordDonation(200, t1);
        d.recordDonation(400, t2);
        assertEquals(600, d.getTotalMl());
        assertEquals(t2, d.getLastDonateAt());
        assertThrows(BizException.class, () -> d.recordDonation(0, t2));
        assertThrows(BizException.class, () -> d.recordDonation(200, null));
    }
}
