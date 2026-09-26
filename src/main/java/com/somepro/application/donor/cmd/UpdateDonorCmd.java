package com.somepro.application.donor.cmd;

import com.somepro.domain.donor.model.Gender;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;

/** 修改献血者档案命令（应用层入参）。累计献血量/最近献血时刻不在这里，由献血业务回写。 */
public record UpdateDonorCmd(String name,
                             Gender gender,
                             BloodGroup bloodGroup,
                             RhFactor rh,
                             String phone,
                             String status) {
}
