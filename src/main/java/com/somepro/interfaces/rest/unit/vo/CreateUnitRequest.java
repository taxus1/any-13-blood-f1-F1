package com.somepro.interfaces.rest.unit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登记血袋请求体。
 *
 * 刻意没有 bloodGroup / rh：血型不另填，由系统按 donorId 读献血者档案带出。
 * 编号、状态也不在请求里：编号系统取号，新袋默认 COLLECTED 已采集待检。
 * 时间格式 yyyy-MM-dd HH:mm:ss（全局 Jackson 已配，这里注解显式声明契约）。
 */
public record CreateUnitRequest(
        @NotNull(message = "献血者 id 不能为空")
        Long donorId,

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
        String storageTemp)
        implements Serializable {
}
