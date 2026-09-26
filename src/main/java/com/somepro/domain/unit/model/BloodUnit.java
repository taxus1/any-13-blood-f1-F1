package com.somepro.domain.unit.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 血袋聚合根（领域层）。
 *
 * 采完一袋登记一条；编号 {@code unitNo} 全局唯一（BU-yyyy-序号）。
 * 血型不另填：建档时由献血者档案带出（{@link #collect} 强制传入档案上的 ABO/Rh）。
 * 新登记默认状态 COLLECTED 已采集待检。
 *
 * 纯领域对象，不落框架注解：落库形状见 BloodUnitPO，对外形状见 BloodUnitVO。
 */
@Getter
@Setter
public class BloodUnit extends BaseEntity {

    /** 编号格式：BU-2026-000001（年份 + 6 位序号，序号溢出后自然加宽）。 */
    public static final Pattern NO_PATTERN = Pattern.compile("^BU-\\d{4}-\\d{6,}$");

    private Long id;

    private String unitNo;

    /** 献血者 id（谁献的那袋）。 */
    private Long donorId;

    private ComponentType component;

    /** ABO 血型：从献血者档案带出，不接受外部另填。 */
    private BloodGroup bloodGroup;

    /** Rh 因子：从献血者档案带出，不接受外部另填。 */
    private RhFactor rh;

    private int volumeMl;

    private LocalDateTime collectedAt;

    private LocalDateTime expireAt;

    private StorageTemp storageTemp;

    private UnitStatus status;

    /**
     * 工厂方法：登记一袋新采的血。
     *
     * @param donorId     献血者 id
     * @param group       档案上的 ABO 血型（带出来，不另填）
     * @param rh          档案上的 Rh 因子（带出来，不另填）
     * @param component   成分
     * @param volumeMl    本袋容量（毫升）
     * @param collectedAt 采集时刻
     * @param expireAt    失效时刻
     * @param storageTemp 保存温度档
     */
    public static BloodUnit collect(Long donorId, BloodGroup group, RhFactor rh,
                                    ComponentType component, int volumeMl,
                                    LocalDateTime collectedAt, LocalDateTime expireAt,
                                    StorageTemp storageTemp) {
        if (donorId == null) {
            throw new BizException("血袋必须关联献血者");
        }
        if (group == null) {
            throw new BizException("ABO 血型必须由献血者档案带出");
        }
        if (rh == null) {
            throw new BizException("Rh 因子必须由献血者档案带出");
        }
        if (component == null) {
            throw new BizException("血液成分不能为空");
        }
        if (volumeMl <= 0) {
            throw new BizException("血袋容量必须为正数");
        }
        if (collectedAt == null) {
            throw new BizException("采集时刻不能为空");
        }
        if (expireAt == null) {
            throw new BizException("失效时刻不能为空");
        }
        if (!expireAt.isAfter(collectedAt)) {
            throw new BizException("失效时刻必须晚于采集时刻");
        }
        if (storageTemp == null) {
            throw new BizException("保存温度档不能为空");
        }
        BloodUnit unit = new BloodUnit();
        unit.donorId = donorId;
        unit.bloodGroup = group;
        unit.rh = rh;
        unit.component = component;
        unit.volumeMl = volumeMl;
        unit.collectedAt = collectedAt;
        unit.expireAt = expireAt;
        unit.storageTemp = storageTemp;
        unit.status = UnitStatus.COLLECTED;
        return unit;
    }

    /** 分配全局唯一编号（由仓储按年取号），只允许分配一次。 */
    public void assignUnitNo(String unitNo) {
        if (unitNo == null || unitNo.isBlank()) {
            throw new BizException("血袋编号不能为空");
        }
        if (!NO_PATTERN.matcher(unitNo).matches()) {
            throw new BizException("血袋编号格式非法：" + unitNo + "，应为 BU-年份-序号，如 BU-2026-000001");
        }
        if (this.unitNo != null) {
            throw new BizException("血袋编号已分配，不允许更改");
        }
        this.unitNo = unitNo;
    }

    /**
     * 改登记信息：成分、容量、采集/失效时刻、温度档可更正；血型不在这里传，
     * 改档案血型不会回溯改动已采血袋，保证「档案怎么写、这袋血就照什么样」只在建档那一刻生效。
     */
    public void updateInfo(ComponentType component, int volumeMl,
                           LocalDateTime collectedAt, LocalDateTime expireAt,
                           StorageTemp storageTemp) {
        if (component == null) {
            throw new BizException("血液成分不能为空");
        }
        if (volumeMl <= 0) {
            throw new BizException("血袋容量必须为正数");
        }
        if (collectedAt == null) {
            throw new BizException("采集时刻不能为空");
        }
        if (expireAt == null) {
            throw new BizException("失效时刻不能为空");
        }
        if (!expireAt.isAfter(collectedAt)) {
            throw new BizException("失效时刻必须晚于采集时刻");
        }
        if (storageTemp == null) {
            throw new BizException("保存温度档不能为空");
        }
        this.component = component;
        this.volumeMl = volumeMl;
        this.collectedAt = collectedAt;
        this.expireAt = expireAt;
        this.storageTemp = storageTemp;
    }

    /** 调整状态：检测、在库、发放、报废等用例驱动，基础档案维护阶段也允许直接更正。 */
    public void changeStatus(UnitStatus status) {
        if (status == null) {
            throw new BizException("血袋状态不能为空");
        }
        this.status = status;
    }
}
