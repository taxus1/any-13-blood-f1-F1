package com.somepro.interfaces.rest.donor.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 献血者对外 VO（不可变 record）。
 * 暴露业务编号与全部底账字段；不含 delFlag 及审计字段（内部维护信息不进 API 契约）。
 */
public record DonorVO(Long id,
                      String donorNo,
                      String name,
                      String gender,
                      String bloodGroup,
                      String rh,
                      String phone,
                      Integer totalMl,
                      LocalDateTime lastDonateAt,
                      String status,
                      LocalDateTime createTime)
        implements Serializable {
}
