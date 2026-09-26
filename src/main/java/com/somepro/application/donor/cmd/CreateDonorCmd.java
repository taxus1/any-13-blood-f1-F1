package com.somepro.application.donor.cmd;

import com.somepro.domain.donor.model.Gender;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;

/** 登记献血者命令（应用层入参，字段都已解析为领域类型）。 */
public record CreateDonorCmd(String name,
                             Gender gender,
                             BloodGroup bloodGroup,
                             RhFactor rh,
                             String phone) {
}
