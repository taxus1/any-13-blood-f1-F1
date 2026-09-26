package com.somepro.application.donor.cmd;

import com.somepro.domain.donor.model.Gender;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;

/**
 * 登记献血者命令（应用层入参，字段都已解析为领域类型）。
 * donorNo 可空：调用方指定了就用指定的（撞号即拒），没指定由系统按年取号。
 */
public record CreateDonorCmd(String name,
                             Gender gender,
                             BloodGroup bloodGroup,
                             RhFactor rh,
                             String phone,
                             String donorNo) {
}
