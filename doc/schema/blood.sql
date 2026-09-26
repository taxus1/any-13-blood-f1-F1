-- any-13-blood · 中心血站采供血与供血调度 · 建表 SQL
-- 字符集 utf8mb4，时区 Asia/Shanghai。create 阶段建好，模型只写业务代码，不碰建表。
-- 列名即契约：del_flag 由 @TableLogic 自动拼接（查询带 del_flag=0，删除置 1），
-- create_by/update_by/create_time/update_time 由 AutoFillMetaObjectHandler 自动填充，业务代码不要手写。
-- 主键 id 由应用侧雪花分配（IdType.INPUT），不依赖自增。

-- 1) 献血者档案
CREATE TABLE IF NOT EXISTS t_blood_donor (
    id             BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    donor_no       VARCHAR(32) NOT NULL COMMENT '献血者编号，全局唯一（如 DNR-2026-0001）',
    donor_name     VARCHAR(64) NOT NULL COMMENT '姓名',
    gender         VARCHAR(8)  NOT NULL COMMENT 'MALE 男 / FEMALE 女',
    blood_group    VARCHAR(4)  NOT NULL COMMENT 'ABO 血型 A/B/AB/O',
    rh             VARCHAR(8)  NOT NULL COMMENT 'Rh 因子 POSITIVE 阳性 / NEGATIVE 阴性',
    phone          VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    total_ml       INT         NOT NULL DEFAULT 0 COMMENT '累计献血量（毫升）',
    last_donate_at DATETIME    DEFAULT NULL COMMENT '最近一次献血时刻',
    status         VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 可献 / DEFERRED 暂缓 / BLOCKED 屏蔽',
    del_flag       TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by      VARCHAR(64) DEFAULT NULL,
    create_time    DATETIME    DEFAULT NULL,
    update_by      VARCHAR(64) DEFAULT NULL,
    update_time    DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_donor_no (donor_no),
    KEY idx_group_rh (blood_group, rh),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='献血者档案';

-- 2) 血液（血袋）
CREATE TABLE IF NOT EXISTS t_blood_unit (
    id           BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    unit_no      VARCHAR(32) NOT NULL COMMENT '血袋编号，全局唯一（如 BU-2026-000001）',
    donor_id     BIGINT      NOT NULL COMMENT '献血者 id（t_blood_donor.id）',
    component    VARCHAR(16) NOT NULL COMMENT '成分 WHOLE 全血 / RBC 红细胞 / PLASMA 血浆 / PLATELET 血小板',
    blood_group  VARCHAR(4)  NOT NULL COMMENT 'ABO 血型 A/B/AB/O',
    rh           VARCHAR(8)  NOT NULL COMMENT 'Rh 因子 POSITIVE 阳性 / NEGATIVE 阴性',
    volume_ml    INT         NOT NULL DEFAULT 0 COMMENT '容量（毫升）',
    collected_at DATETIME    NOT NULL COMMENT '采集时刻',
    expire_at    DATETIME    NOT NULL COMMENT '失效时刻',
    storage_temp VARCHAR(16) DEFAULT NULL COMMENT '保存温度档 2-6 / 20-24',
    status       VARCHAR(16) NOT NULL DEFAULT 'COLLECTED' COMMENT 'COLLECTED 已采集待检 / QUALIFIED 检测合格 / REJECTED 检测不合格 / IN_STOCK 在库 / ISSUED 已发放 / DISCARDED 已报废',
    del_flag     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by    VARCHAR(64) DEFAULT NULL,
    create_time  DATETIME    DEFAULT NULL,
    update_by    VARCHAR(64) DEFAULT NULL,
    update_time  DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_unit_no (unit_no),
    KEY idx_donor (donor_id),
    KEY idx_group (blood_group, rh, component),
    KEY idx_status (status),
    KEY idx_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血液';

-- 3) 血液检测（每袋血按轮次检测，初检 + 复检）
CREATE TABLE IF NOT EXISTS t_blood_test (
    id         BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    test_no    VARCHAR(32) NOT NULL COMMENT '检测单号，全局唯一（如 TS-2026-0001）',
    unit_id    BIGINT      NOT NULL COMMENT '血袋 id（t_blood_unit.id）',
    round_no   INT         NOT NULL COMMENT '轮次：1 初检 / 2 复检',
    hbv        VARCHAR(8)  NOT NULL DEFAULT 'NEG' COMMENT '乙肝标志 NEG 阴 / POS 阳',
    hcv        VARCHAR(8)  NOT NULL DEFAULT 'NEG' COMMENT '丙肝标志 NEG 阴 / POS 阳',
    hiv        VARCHAR(8)  NOT NULL DEFAULT 'NEG' COMMENT '艾滋标志 NEG 阴 / POS 阳',
    syphilis   VARCHAR(8)  NOT NULL DEFAULT 'NEG' COMMENT '梅毒标志 NEG 阴 / POS 阳',
    alt_value  INT         DEFAULT NULL COMMENT '转氨酶数值',
    conclusion VARCHAR(8)  NOT NULL COMMENT 'PASS 合格 / FAIL 不合格',
    tested_at  DATETIME    DEFAULT NULL COMMENT '检测时刻',
    tester     VARCHAR(64) DEFAULT NULL COMMENT '检测人（登录账号）',
    del_flag   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by  VARCHAR(64) DEFAULT NULL,
    create_time DATETIME   DEFAULT NULL,
    update_by  VARCHAR(64) DEFAULT NULL,
    update_time DATETIME   DEFAULT NULL,
    UNIQUE KEY uk_test_no (test_no),
    UNIQUE KEY uk_unit_round (unit_id, round_no),
    KEY idx_unit (unit_id),
    KEY idx_conclusion (conclusion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血液检测';

-- 4) 交叉配血申请
CREATE TABLE IF NOT EXISTS t_crossmatch (
    id           BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    request_no   VARCHAR(32) NOT NULL COMMENT '配血申请单号，全局唯一（如 CM-2026-0001）',
    unit_id      BIGINT      NOT NULL COMMENT '血袋 id（t_blood_unit.id）',
    apply_dept   VARCHAR(64) NOT NULL COMMENT '申请科室',
    patient_no   VARCHAR(32) NOT NULL COMMENT '患者住院号',
    patient_name VARCHAR(64) DEFAULT NULL COMMENT '患者姓名',
    patient_group VARCHAR(4) NOT NULL COMMENT '患者 ABO 血型 A/B/AB/O',
    patient_rh   VARCHAR(8)  NOT NULL COMMENT '患者 Rh 因子 POSITIVE / NEGATIVE',
    component    VARCHAR(16) NOT NULL COMMENT '申请成分 WHOLE/RBC/PLASMA/PLATELET',
    method       VARCHAR(8)  NOT NULL DEFAULT 'MAJOR' COMMENT '配血方法 MAJOR 主侧 / MINOR 次侧 / BOTH 双侧',
    result       VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING 待配 / COMPATIBLE 相合 / INCOMPATIBLE 不合',
    apply_reason VARCHAR(255) DEFAULT NULL COMMENT '申请原因',
    requested_at DATETIME    DEFAULT NULL COMMENT '申请时刻',
    matched_at   DATETIME    DEFAULT NULL COMMENT '配血时刻',
    matched_by   VARCHAR(64) DEFAULT NULL COMMENT '配血人（登录账号）',
    del_flag     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by    VARCHAR(64) DEFAULT NULL,
    create_time  DATETIME    DEFAULT NULL,
    update_by    VARCHAR(64) DEFAULT NULL,
    update_time  DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_request_no (request_no),
    KEY idx_unit (unit_id),
    KEY idx_patient (patient_no),
    KEY idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交叉配血申请';

-- 5) 血液发放（发血、退血、输注反应回填）
CREATE TABLE IF NOT EXISTS t_blood_issue (
    id            BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    issue_no      VARCHAR(32) NOT NULL COMMENT '发血单号，全局唯一（如 IS-2026-0001）',
    unit_id       BIGINT      NOT NULL COMMENT '血袋 id（t_blood_unit.id）',
    crossmatch_id BIGINT      DEFAULT NULL COMMENT '配血申请 id（t_crossmatch.id）',
    receive_dept  VARCHAR(64) NOT NULL COMMENT '领血科室',
    issued_at     DATETIME    DEFAULT NULL COMMENT '发放时刻',
    status        VARCHAR(16) NOT NULL DEFAULT 'ISSUED' COMMENT 'ISSUED 已发放 / RETURNED 已退回 / TRANSFUSED 已输注',
    return_reason VARCHAR(255) DEFAULT NULL COMMENT '退回原因',
    returned_at   DATETIME    DEFAULT NULL COMMENT '退回时刻',
    reaction      VARCHAR(16) DEFAULT NULL COMMENT '输注反应 NONE 无 / MILD 轻度 / SEVERE 重度',
    del_flag      TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by     VARCHAR(64) DEFAULT NULL,
    create_time   DATETIME    DEFAULT NULL,
    update_by     VARCHAR(64) DEFAULT NULL,
    update_time   DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_issue_no (issue_no),
    KEY idx_unit (unit_id),
    KEY idx_status (status),
    KEY idx_receive (receive_dept)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血液发放';

-- 6) 血液报废
CREATE TABLE IF NOT EXISTS t_blood_discard (
    id           BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    discard_no   VARCHAR(32) NOT NULL COMMENT '报废单号，全局唯一（如 DC-2026-0001）',
    unit_id      BIGINT      NOT NULL COMMENT '血袋 id（t_blood_unit.id）',
    reason       VARCHAR(16) NOT NULL COMMENT '报废原因 EXPIRED 过期 / DAMAGED 破损 / REACTIVE 反应 / UNQUALIFIED 不合格 / OTHER 其他',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '说明',
    discarded_at DATETIME    DEFAULT NULL COMMENT '报废时刻',
    del_flag     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by    VARCHAR(64) DEFAULT NULL,
    create_time  DATETIME    DEFAULT NULL,
    update_by    VARCHAR(64) DEFAULT NULL,
    update_time  DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_discard_no (discard_no),
    KEY idx_unit (unit_id),
    KEY idx_reason (reason)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血液报废';

-- 7) 库存预警规则（按血型 + Rh + 成分设下限）
CREATE TABLE IF NOT EXISTS t_blood_stock (
    id          BIGINT      NOT NULL PRIMARY KEY COMMENT '雪花 ID，应用层分配',
    blood_group VARCHAR(4)  NOT NULL COMMENT 'ABO 血型 A/B/AB/O',
    rh          VARCHAR(8)  NOT NULL COMMENT 'Rh 因子 POSITIVE / NEGATIVE',
    component   VARCHAR(16) NOT NULL COMMENT '成分 WHOLE/RBC/PLASMA/PLATELET',
    min_level   INT         NOT NULL DEFAULT 5 COMMENT '在库下限',
    status      VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED 启用 / DISABLED 停用',
    del_flag    TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    create_by   VARCHAR(64) DEFAULT NULL,
    create_time DATETIME    DEFAULT NULL,
    update_by   VARCHAR(64) DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_group_component (blood_group, rh, component),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警规则';
