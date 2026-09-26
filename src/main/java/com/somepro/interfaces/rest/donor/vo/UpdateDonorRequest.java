package com.somepro.interfaces.rest.donor.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * 修改献血者档案请求体。
 * 编号不可改（路径上的 id 定位记录）；累计量/最近献血时刻不由编辑接口改写。
 * status 可空：不传表示只改资料、不动状态。
 */
public record UpdateDonorRequest(
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

        @Pattern(regexp = "ACTIVE|DEFERRED|BLOCKED|", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "状态取值 ACTIVE 可献 / DEFERRED 暂缓 / BLOCKED 屏蔽")
        String status)
        implements Serializable {
}
