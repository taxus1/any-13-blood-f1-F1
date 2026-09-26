package com.somepro.interfaces.rest.donor.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * 登记献血者请求体。
 * 枚举码统一大写（MALE/FEMALE、A/B/AB/O、POSITIVE/NEGATIVE）；非法取值由领域枚举解析给出明确提示。
 * donorNo 可空：对照纸质单指定了就用指定的（撞号挡回）；不填由系统按年取号。
 * 累计量、最近献血时刻、状态均不在请求里：前两项随献血业务回写，新建默认 ACTIVE。
 */
public record CreateDonorRequest(
        @NotBlank(message = "姓名不能为空")
        String name,

        @NotBlank(message = "性别不能为空")
        @Pattern(regexp = "MALE|FEMALE", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "性别取值 MALE 男 / FEMALE 女")
        String gender,

        @NotBlank(message = "ABO 血型不能为空")
        @Pattern(regexp = "A|B|AB|O", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "ABO 血型取值 A / B / AB / O")
        String bloodGroup,

        @NotBlank(message = "Rh 因子不能为空")
        @Pattern(regexp = "POSITIVE|NEGATIVE", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Rh 因子取值 POSITIVE 阳性 / NEGATIVE 阴性")
        String rh,

        @Pattern(regexp = "^$|^[0-9+\\-() ]{3,20}$", message = "联系电话格式不正确")
        String phone,

        @Pattern(regexp = "DNR-\\d{4}-\\d{4,}", message = "编号格式应为 DNR-年份-序号，如 DNR-2026-0001")
        String donorNo)
        implements Serializable {
}
