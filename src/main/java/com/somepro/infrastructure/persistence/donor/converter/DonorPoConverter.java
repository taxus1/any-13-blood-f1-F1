package com.somepro.infrastructure.persistence.donor.converter;

import com.somepro.domain.donor.model.Donor;
import com.somepro.domain.donor.model.DonorStatus;
import com.somepro.domain.donor.model.Gender;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;
import com.somepro.infrastructure.persistence.donor.po.DonorPO;

/**
 * DonorPO（表）↔ Donor（领域）转换器（基础设施层），PO 与领域间唯一转换入口。
 *
 * 枚举以名称字符串落库（列都是 VARCHAR）：MALE/FEMALE、A/B/AB/O、POSITIVE/NEGATIVE、ACTIVE/...。
 * 存量数据可能是人工录入的，读库时对枚举列做容错解析；读不出来说明底账本身就是脏数据，直接抛出。
 */
public final class DonorPoConverter {

    private DonorPoConverter() {
    }

    public static DonorPO toPo(Donor d) {
        DonorPO po = new DonorPO();
        po.setId(d.getId());
        po.setDonorNo(d.getDonorNo());
        po.setDonorName(d.getName());
        po.setGender(d.getGender() == null ? null : d.getGender().name());
        po.setBloodGroup(d.getBloodGroup() == null ? null : d.getBloodGroup().name());
        po.setRh(d.getRh() == null ? null : d.getRh().name());
        po.setPhone(d.getPhone());
        po.setTotalMl(d.getTotalMl());
        po.setLastDonateAt(d.getLastDonateAt());
        po.setStatus(d.getStatus() == null ? null : d.getStatus().name());
        po.setDelFlag(d.getDelFlag());
        po.setCreateBy(d.getCreateBy());
        po.setCreateTime(d.getCreateTime());
        po.setUpdateBy(d.getUpdateBy());
        po.setUpdateTime(d.getUpdateTime());
        return po;
    }

    public static Donor toDomain(DonorPO po) {
        Donor d = new Donor();
        d.setId(po.getId());
        d.setDonorNo(po.getDonorNo());
        d.setName(po.getDonorName());
        d.setGender(po.getGender() == null ? null : Gender.valueOf(po.getGender().trim().toUpperCase()));
        d.setBloodGroup(po.getBloodGroup() == null ? null : BloodGroup.valueOf(po.getBloodGroup().trim().toUpperCase()));
        d.setRh(po.getRh() == null ? null : RhFactor.valueOf(po.getRh().trim().toUpperCase()));
        d.setPhone(po.getPhone());
        d.setTotalMl(po.getTotalMl() == null ? 0 : po.getTotalMl());
        d.setLastDonateAt(po.getLastDonateAt());
        d.setStatus(po.getStatus() == null ? DonorStatus.ACTIVE : DonorStatus.valueOf(po.getStatus().trim().toUpperCase()));
        d.setDelFlag(po.getDelFlag());
        d.setCreateBy(po.getCreateBy());
        d.setCreateTime(po.getCreateTime());
        d.setUpdateBy(po.getUpdateBy());
        d.setUpdateTime(po.getUpdateTime());
        return d;
    }
}
