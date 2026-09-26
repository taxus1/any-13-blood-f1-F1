package com.somepro.infrastructure.persistence.unit.po;

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
 * t_blood_unit 表的持久化对象（PO，基础设施层）。
 *
 * 只描述「表长什么样」，不放业务规则（规则在领域对象 BloodUnit）。
 * component / blood_group / rh / status 存枚举名称字符串；
 * storage_temp 存档位编码（"2-6" / "20-24"），与单据、接口上的写法一致。
 */
@Getter
@Setter
@TableName("t_blood_unit")
public class BloodUnitPO extends BasePO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("unit_no")
    private String unitNo;

    @TableField("donor_id")
    private Long donorId;

    @TableField("component")
    private String component;

    @TableField("blood_group")
    private String bloodGroup;

    @TableField("rh")
    private String rh;

    @TableField("volume_ml")
    private Integer volumeMl;

    @TableField("collected_at")
    private LocalDateTime collectedAt;

    @TableField("expire_at")
    private LocalDateTime expireAt;

    /** 温度档可空（表上允许）；ALWAYS 保证清空也能落库。 */
    @TableField(value = "storage_temp", updateStrategy = FieldStrategy.ALWAYS)
    private String storageTemp;

    @TableField("status")
    private String status;
}
