package com.somepro.application.donor.cmd;

import com.somepro.domain.donor.model.Gender;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;

/**
 * 修改献血者档案命令（应用层入参）。累计献血量/最近献血时刻不在这里，由献血业务回写。
 * donorNo 可空：传了表示要换号（撞别人在用的号会被挡回，原档案不动），不传表示沿用原号。
 */
public record UpdateDonorCmd(String name,
                             Gender gender,
                             BloodGroup bloodGroup,
                             RhFactor rh,
                             String phone,
                             String status,
                             String donorNo) {
}
