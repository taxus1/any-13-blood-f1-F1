package com.somepro.infrastructure.persistence.unit.converter;

import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;
import com.somepro.domain.unit.model.BloodUnit;
import com.somepro.domain.unit.model.ComponentType;
import com.somepro.domain.unit.model.StorageTemp;
import com.somepro.domain.unit.model.UnitListItem;
import com.somepro.domain.unit.model.UnitStatus;
import com.somepro.infrastructure.persistence.unit.po.BloodUnitPO;
import com.somepro.infrastructure.persistence.unit.po.UnitListRowPO;

/**
 * BloodUnitPO / UnitListRowPO（表）↔ 血袋领域模型转换器（基础设施层）。
 *
 * 枚举列容错解析（存量数据可能为人工录入）；storage_temp 存/取档位编码 2-6、20-24。
 */
public final class BloodUnitPoConverter {

    private BloodUnitPoConverter() {
    }

    public static BloodUnitPO toPo(BloodUnit u) {
        BloodUnitPO po = new BloodUnitPO();
        po.setId(u.getId());
        po.setUnitNo(u.getUnitNo());
        po.setDonorId(u.getDonorId());
        po.setComponent(u.getComponent() == null ? null : u.getComponent().name());
        po.setBloodGroup(u.getBloodGroup() == null ? null : u.getBloodGroup().name());
        po.setRh(u.getRh() == null ? null : u.getRh().name());
        po.setVolumeMl(u.getVolumeMl());
        po.setCollectedAt(u.getCollectedAt());
        po.setExpireAt(u.getExpireAt());
        po.setStorageTemp(u.getStorageTemp() == null ? null : u.getStorageTemp().code());
        po.setStatus(u.getStatus() == null ? null : u.getStatus().name());
        po.setDelFlag(u.getDelFlag());
        po.setCreateBy(u.getCreateBy());
        po.setCreateTime(u.getCreateTime());
        po.setUpdateBy(u.getUpdateBy());
        po.setUpdateTime(u.getUpdateTime());
        return po;
    }

    public static BloodUnit toDomain(BloodUnitPO po) {
        BloodUnit u = new BloodUnit();
        u.setId(po.getId());
        u.setUnitNo(po.getUnitNo());
        u.setDonorId(po.getDonorId());
        u.setComponent(valueOf(ComponentType.class, po.getComponent()));
        u.setBloodGroup(valueOf(BloodGroup.class, po.getBloodGroup()));
        u.setRh(valueOf(RhFactor.class, po.getRh()));
        u.setVolumeMl(po.getVolumeMl() == null ? 0 : po.getVolumeMl());
        u.setCollectedAt(po.getCollectedAt());
        u.setExpireAt(po.getExpireAt());
        u.setStorageTemp(po.getStorageTemp() == null ? null : StorageTemp.parse(po.getStorageTemp()));
        u.setStatus(po.getStatus() == null ? UnitStatus.COLLECTED : valueOf(UnitStatus.class, po.getStatus()));
        u.setDelFlag(po.getDelFlag());
        u.setCreateBy(po.getCreateBy());
        u.setCreateTime(po.getCreateTime());
        u.setUpdateBy(po.getUpdateBy());
        u.setUpdateTime(po.getUpdateTime());
        return u;
    }

    /** join 列表行 → 领域列表值对象。档案被软删时献血者编号/姓名为 null，血袋行仍照常返回。 */
    public static UnitListItem toListItem(UnitListRowPO row) {
        return new UnitListItem(
                row.getId(),
                row.getUnitNo(),
                row.getDonorId(),
                row.getDonorNo(),
                row.getDonorName(),
                valueOf(ComponentType.class, row.getComponent()),
                valueOf(BloodGroup.class, row.getBloodGroup()),
                valueOf(RhFactor.class, row.getRh()),
                row.getVolumeMl() == null ? 0 : row.getVolumeMl(),
                row.getCollectedAt(),
                row.getExpireAt(),
                row.getStorageTemp(),
                valueOf(UnitStatus.class, row.getStatus()));
    }

    private static <E extends Enum<E>> E valueOf(Class<E> enumType, String value) {
        return value == null ? null : Enum.valueOf(enumType, value.trim().toUpperCase());
    }
}
