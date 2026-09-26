package com.somepro.interfaces.rest.unit.converter;

import com.somepro.application.unit.cmd.CreateUnitCmd;
import com.somepro.application.unit.cmd.UpdateUnitCmd;
import com.somepro.domain.unit.model.BloodUnit;
import com.somepro.domain.unit.model.ComponentType;
import com.somepro.domain.unit.model.StorageTemp;
import com.somepro.domain.unit.model.UnitListItem;
import com.somepro.interfaces.rest.unit.vo.BloodUnitVO;
import com.somepro.interfaces.rest.unit.vo.CreateUnitRequest;
import com.somepro.interfaces.rest.unit.vo.UpdateUnitRequest;

/**
 * 血袋接口层转换器：请求体 → 应用命令，血袋聚合 / 列表行 → VO。
 * 血型字段在所有转换里都不出现 —— 它只从献血者档案带出。
 */
public final class BloodUnitVoConverter {

    private BloodUnitVoConverter() {
    }

    public static CreateUnitCmd toCmd(CreateUnitRequest req) {
        return new CreateUnitCmd(
                req.donorId(),
                ComponentType.parse(req.component()),
                req.volumeMl(),
                req.collectedAt(),
                req.expireAt(),
                StorageTemp.parse(req.storageTemp()));
    }

    public static UpdateUnitCmd toCmd(UpdateUnitRequest req) {
        return new UpdateUnitCmd(
                ComponentType.parse(req.component()),
                req.volumeMl(),
                req.collectedAt(),
                req.expireAt(),
                StorageTemp.parse(req.storageTemp()),
                blankToNull(req.status()));
    }

    /** 详情：只有血袋自身信息，献血者编号/姓名留空（前端需要可另调名单接口）。 */
    public static BloodUnitVO toVo(BloodUnit u) {
        return new BloodUnitVO(
                u.getId(),
                u.getUnitNo(),
                u.getDonorId(),
                null,
                null,
                u.getComponent() == null ? null : u.getComponent().name(),
                u.getBloodGroup() == null ? null : u.getBloodGroup().name(),
                u.getRh() == null ? null : u.getRh().name(),
                u.getVolumeMl(),
                u.getCollectedAt(),
                u.getExpireAt(),
                u.getStorageTemp() == null ? null : u.getStorageTemp().code(),
                u.getStatus() == null ? null : u.getStatus().name(),
                u.getCreateTime());
    }

    /** 列表行：带献血者编号/姓名。 */
    public static BloodUnitVO toVo(UnitListItem item) {
        return new BloodUnitVO(
                item.id(),
                item.unitNo(),
                item.donorId(),
                item.donorNo(),
                item.donorName(),
                item.component() == null ? null : item.component().name(),
                item.bloodGroup() == null ? null : item.bloodGroup().name(),
                item.rh() == null ? null : item.rh().name(),
                item.volumeMl(),
                item.collectedAt(),
                item.expireAt(),
                item.storageTemp(),
                item.status() == null ? null : item.status().name(),
                null);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
