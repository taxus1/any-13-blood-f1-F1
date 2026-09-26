package com.somepro.application.unit.cmd;

import com.somepro.domain.unit.model.ComponentType;
import com.somepro.domain.unit.model.StorageTemp;

import java.time.LocalDateTime;

/** 修改血袋登记信息命令（应用层入参）。血型不可改（建档时随档案带出），状态可选更正。 */
public record UpdateUnitCmd(ComponentType component,
                            int volumeMl,
                            LocalDateTime collectedAt,
                            LocalDateTime expireAt,
                            StorageTemp storageTemp,
                            String status) {
}
