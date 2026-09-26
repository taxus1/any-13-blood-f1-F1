package com.somepro.interfaces.rest.unit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 修改血袋登记信息请求体。
 * 血型不允许改（建档时随档案带出）；献血者也不允许改（哪袋就是谁献的）。
 * status 可空：不传表示只改登记信息，后续检测/发放等用例会有专门的状态接口。
 */
public record UpdateUnitRequest(
        @NotNull(message = "血液成分不能为空")
        @Pattern(regexp = "WHOLE|RBC|PLASMA|PLATELET", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "成分取值 WHOLE 全血 / RBC 红细胞 / PLASMA 血浆 / PLATELET 血小板")
        String component,

        @Positive(message = "血袋容量必须为正整数")
        @NotNull(message = "容量不能为空")
        Integer volumeMl,

        @NotNull(message = "采集时刻不能为空，格式 yyyy-MM-dd HH:mm:ss")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime collectedAt,

        @NotNull(message = "失效时刻不能为空，格式 yyyy-MM-dd HH:mm:ss")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expireAt,

        @NotNull(message = "保存温度档不能为空")
        @Pattern(regexp = "2-6|20-24", message = "保存温度档取值 2-6 冷藏 / 20-24 室温")
        String storageTemp,

        @Pattern(regexp = "COLLECTED|QUALIFIED|REJECTED|IN_STOCK|ISSUED|DISCARDED|",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "状态取值 COLLECTED / QUALIFIED / REJECTED / IN_STOCK / ISSUED / DISCARDED")
        String status)
        implements Serializable {
}
