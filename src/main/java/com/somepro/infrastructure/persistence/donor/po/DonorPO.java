package com.somepro.infrastructure.persistence.donor.po;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.somepro.infrastructure.persistence.base.BasePO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * t_blood_donor 表的持久化对象（PO，基础设施层）。
 *
 * 只描述「表长什么样」，不放业务规则（规则在领域对象 Donor）。
 * gender / blood_group / rh / status 列是 VARCHAR，统一存枚举的名称（MALE、A、POSITIVE…），
 * 由 DonorPoConverter 与领域枚举互转。
 */
@Getter
@Setter
@TableName("t_blood_donor")
public class DonorPO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("donor_no")
    private String donorNo;

    @TableField("donor_name")
    private String donorName;

    @TableField("gender")
    private String gender;

    @TableField("blood_group")
    private String bloodGroup;

    @TableField("rh")
    private String rh;

    /** 联系电话可空；updateStrategy=ALWAYS 保证「清空电话」也能落库（默认 NOT_NULL 策略会丢掉 null）。 */
    @TableField(value = "phone", updateStrategy = FieldStrategy.ALWAYS)
    private String phone;

    @TableField("total_ml")
    private Integer totalMl;

    @TableField("last_donate_at")
    private LocalDateTime lastDonateAt;

    @TableField("status")
    private String status;
}
