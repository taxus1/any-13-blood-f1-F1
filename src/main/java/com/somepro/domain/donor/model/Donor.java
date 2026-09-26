package com.somepro.domain.donor.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 献血者档案聚合根（领域层）。
 *
 * 一个人对应一条档案；编号 {@code donorNo} 全局唯一（DNR-yyyy-序号），一个号只归一个人。
 * 血型（ABO + Rh）是档案的固有属性，血袋建档时从这里原样带出。
 *
 * 纯领域对象，不带任何持久化/接口注解：落库形状见 DonorPO，对外形状见 DonorVO。
 */
@Getter
@Setter
public class Donor extends BaseEntity {

    /** 编号格式：DNR-2026-0001（年份 + 4 位序号，序号溢出后自然加宽）。 */
    public static final Pattern NO_PATTERN = Pattern.compile("^DNR-\\d{4}-\\d{4,}$");

    private Long id;

    private String donorNo;

    private String name;

    private Gender gender;

    private BloodGroup bloodGroup;

    private RhFactor rh;

    private String phone;

    /** 累计献血量（毫升），只由献血业务（recordDonation）累加，不接受外部直接改写。 */
    private int totalMl;

    /** 最近一次献血时刻，未献过为 null。 */
    private LocalDateTime lastDonateAt;

    private DonorStatus status;

    /**
     * 工厂方法：登记新献血者。初始不变量：累计 0ml、无最近献血时刻、状态可献 ACTIVE。
     * 编号不在此处生成（需要查库取号），由应用层通过仓储分配后调 {@link #assignDonorNo}。
     */
    public static Donor register(String name, Gender gender, BloodGroup bloodGroup, RhFactor rh, String phone) {
        Donor donor = new Donor();
        donor.updateProfile(name, gender, bloodGroup, rh, phone);
        donor.totalMl = 0;
        donor.lastDonateAt = null;
        donor.status = DonorStatus.ACTIVE;
        return donor;
    }

    /** 分配全局唯一编号（由仓储按年取号），只允许分配一次。 */
    public void assignDonorNo(String donorNo) {
        if (donorNo == null || donorNo.isBlank()) {
            throw new BizException("献血者编号不能为空");
        }
        if (!NO_PATTERN.matcher(donorNo).matches()) {
            throw new BizException("献血者编号格式非法：" + donorNo + "，应为 DNR-年份-序号，如 DNR-2026-0001");
        }
        if (this.donorNo != null) {
            throw new BizException("献血者编号已分配，不允许更改");
        }
        this.donorNo = donorNo;
    }

    /** 改档案：姓名、性别、血型、Rh、联系电话都允许更正（填错了能改）。 */
    public void updateProfile(String name, Gender gender, BloodGroup bloodGroup, RhFactor rh, String phone) {
        if (name == null || name.isBlank()) {
            throw new BizException("献血者姓名不能为空");
        }
        if (gender == null) {
            throw new BizException("性别不能为空");
        }
        if (bloodGroup == null) {
            throw new BizException("ABO 血型不能为空");
        }
        if (rh == null) {
            throw new BizException("Rh 因子不能为空");
        }
        this.name = name.trim();
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.rh = rh;
        this.phone = phone == null ? null : phone.trim();
    }

    /** 调整状态：可献 / 暂缓 / 屏蔽。 */
    public void changeStatus(DonorStatus status) {
        if (status == null) {
            throw new BizException("献血者状态不能为空");
        }
        this.status = status;
    }

    /**
     * 登记一次献血：累加累计量并刷新最近献血时刻。
     * 供后续采血用例调用；档案自身不开放 totalMl / lastDonateAt 的直接改写。
     */
    public void recordDonation(int volumeMl, LocalDateTime donatedAt) {
        if (volumeMl <= 0) {
            throw new BizException("献血量必须为正数");
        }
        if (donatedAt == null) {
            throw new BizException("献血时刻不能为空");
        }
        this.totalMl += volumeMl;
        this.lastDonateAt = donatedAt;
    }
}
