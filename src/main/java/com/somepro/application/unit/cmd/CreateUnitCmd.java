package com.somepro.application.unit.cmd;

import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.unit.model.ComponentType;
import com.somepro.domain.unit.model.StorageTemp;

import java.time.LocalDateTime;

/**
 * 登记血袋命令（应用层入参）。
 *
 * 刻意不含血型字段：血型由应用层按 donorId 读献血者档案带出，调用方另填也不认。
 */
public record CreateUnitCmd(Long donorId,
                            ComponentType component,
                            int volumeMl,
                            LocalDateTime collectedAt,
                            LocalDateTime expireAt,
                            StorageTemp storageTemp) {
}
