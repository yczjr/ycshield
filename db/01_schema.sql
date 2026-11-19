-- ============================================================
-- 蓝盾 BlueShield · 蓝队防守作战协同平台
-- 数据库初始化脚本（MySQL 8.0）  V2.1
-- 与《建设规划任务书》第 7 章数据设计一一对应
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `blued` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `blued`;

-- ---------- 用户与权限 ----------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `username`       varchar(50)  NOT NULL COMMENT '登录账号',
  `password`       varchar(100) NOT NULL COMMENT 'BCrypt 哈希',
  `real_name`      varchar(50)  DEFAULT NULL COMMENT '姓名',
  `phone`          varchar(20)  DEFAULT NULL,
  `status`         tinyint      NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
  `last_login_time` datetime    DEFAULT NULL,
  `pwd_update_time` datetime    DEFAULT NULL,
  `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `code`        varchar(32) NOT NULL COMMENT 'MONITOR/ANALYST/HANDLER/ADMIN',
  `name`        varchar(32) NOT NULL,
  `remark`      varchar(200) DEFAULT NULL,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id`      bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
  `id`         bigint      NOT NULL AUTO_INCREMENT,
  `dict_type`  varchar(32) NOT NULL COMMENT '字典类型',
  `parent_id`  bigint      NOT NULL DEFAULT 0 COMMENT '父项ID，0 为顶级（两级字典用）',
  `code`       varchar(32) NOT NULL COMMENT '编码',
  `name`       varchar(64) NOT NULL COMMENT '名称',
  `sort`       int         NOT NULL DEFAULT 0,
  `enabled`    tinyint     NOT NULL DEFAULT 1,
  `remark`     varchar(200) DEFAULT NULL,
  `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict` (`dict_type`,`parent_id`,`code`),
  KEY `idx_dict_type` (`dict_type`,`parent_id`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典（支持两级）';

-- ---------- 事件主表 ----------
DROP TABLE IF EXISTS `event_main`;
CREATE TABLE `event_main` (
  `id`                     bigint       NOT NULL AUTO_INCREMENT,
  `event_no`               varchar(32)  NOT NULL COMMENT '事件编号 BS-yyyyMMdd-NNNN',
  `title`                  varchar(120) NOT NULL COMMENT '事件标题',
  `status`                 varchar(16)  NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/ANALYZING/HANDLING/REVIEWING/ARCHIVED/CLOSED',
  `level`                  tinyint      NOT NULL DEFAULT 2 COMMENT '1 高危 2 中危 3 低危',
  `attack_type_category`   varchar(32)  NOT NULL COMMENT '攻击类型大类编码',
  `attack_type`            varchar(32)  NOT NULL COMMENT '攻击类型子类编码',
  `attack_phase`           varchar(32)  DEFAULT NULL COMMENT '攻击阶段',
  `attack_direction`       varchar(16)  NOT NULL COMMENT 'OUT2IN/IN2IN/IN2OUT/UNKNOWN',
  `attack_result`          varchar(16)  NOT NULL COMMENT 'SUCCESS/PARTIAL/FAILED/UNKNOWN',
  `event_source`           varchar(16)  NOT NULL DEFAULT 'MONITOR' COMMENT 'MONITOR/DEVICE/INTEL/MANUAL',
  `found_time`             datetime     NOT NULL COMMENT '发现时间',
  `attack_ips`             text         COMMENT '攻击IP，逗号分隔',
  `attack_region`          varchar(32)  DEFAULT NULL COMMENT '攻击源归属地（国内省份 / 境外国家）',
  `attack_country`         varchar(32)  DEFAULT NULL COMMENT '攻击源国家',
  `victim_ips`             text         COMMENT '受害IP，逗号分隔（由告警汇总去重）',
  `asset_name`             varchar(120) DEFAULT NULL COMMENT '受害资产名称（文本，不做绑定）',
  `impact_status`          varchar(16)  DEFAULT NULL COMMENT 'NONE/SUSPECT/AFFECTED/COMPROMISED',
  `business_impact`        varchar(1000) DEFAULT NULL,
  `preliminary_conclusion` text         NOT NULL COMMENT '监测员初步研判结论',
  `analysis_conclusion`    text         COMMENT '分析员研判结论',
  `result_modified`        tinyint      NOT NULL DEFAULT 0 COMMENT '初判是否被修正',
  `modify_reason`          varchar(300) DEFAULT NULL,
  `reporter_id`            bigint       NOT NULL,
  `reporter_name`          varchar(50)  DEFAULT NULL,
  `owner_id`               bigint       DEFAULT NULL COMMENT '研判负责人',
  `owner_name`             varchar(50)  DEFAULT NULL,
  `handler_id`             bigint       DEFAULT NULL COMMENT '处置人',
  `handler_name`           varchar(50)  DEFAULT NULL,
  `submit_time`            datetime     NOT NULL,
  `assign_time`            datetime     DEFAULT NULL,
  `review_time`            datetime     DEFAULT NULL,
  `archived_time`          datetime     DEFAULT NULL,
  `is_important`           tinyint      NOT NULL DEFAULT 0,
  `is_timeout`             tinyint      NOT NULL DEFAULT 0,
  `close_reason`           varchar(300) DEFAULT NULL,
  `create_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`                tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_no` (`event_no`),
  KEY `idx_status` (`status`),
  KEY `idx_found_time` (`found_time`),
  KEY `idx_level` (`level`),
  KEY `idx_type` (`attack_type_category`,`attack_type`),
  KEY `idx_reporter` (`reporter_id`),
  KEY `idx_handler` (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件主表';

-- ---------- 告警记录（一条事件 N 条） ----------
DROP TABLE IF EXISTS `event_alarm`;
CREATE TABLE `event_alarm` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `event_id`    bigint      NOT NULL,
  `seq`         int         NOT NULL DEFAULT 1 COMMENT '事件内序号',
  `device_type` varchar(32) NOT NULL COMMENT '来源设备类型（字典）',
  `device_name` varchar(64)  DEFAULT NULL COMMENT '设备名称/标识',
  `alarm_time`  datetime    NOT NULL,
  `alarm_name`  varchar(120) DEFAULT NULL COMMENT '告警名称/规则',
  `direction`   varchar(16)  DEFAULT NULL COMMENT '方向，空则继承主表',
  `src_ips`     text         COMMENT '源IP',
  `dst_ips`     text         COMMENT '目的IP',
  `victim_ips`  text         COMMENT '受害IP',
  `raw_text`    mediumtext   COMMENT '原始告警报文',
  `create_by`   bigint       DEFAULT NULL,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录';

-- ---------- 证据附件 ----------
DROP TABLE IF EXISTS `event_evidence`;
CREATE TABLE `event_evidence` (
  `id`          bigint      NOT NULL AUTO_INCREMENT,
  `event_id`    bigint      NOT NULL,
  `alarm_id`    bigint       DEFAULT NULL COMMENT '可归属于某条告警',
  `file_name`   varchar(200) NOT NULL,
  `file_path`   varchar(500) NOT NULL,
  `file_size`   bigint       DEFAULT 0,
  `md5`         varchar(64)  DEFAULT NULL,
  `mime`        varchar(100) DEFAULT NULL,
  `source`      varchar(16)  DEFAULT 'UPLOAD' COMMENT 'UPLOAD/PASTE',
  `uploader_id` bigint       DEFAULT NULL,
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`     tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证据附件';

-- ---------- 流转与字段级变更 ----------
DROP TABLE IF EXISTS `event_flow`;
CREATE TABLE `event_flow` (
  `id`            bigint      NOT NULL AUTO_INCREMENT,
  `event_id`      bigint      NOT NULL,
  `from_status`   varchar(16)  DEFAULT NULL,
  `to_status`     varchar(16)  DEFAULT NULL,
  `action`        varchar(32) NOT NULL COMMENT 'CREATE/CLAIM/ASSIGN/DISPOSE/SUBMIT_REVIEW/ARCHIVE/REJECT/CLOSE/MODIFY',
  `opinion`       varchar(2000) DEFAULT NULL,
  `field_name`    varchar(64)  DEFAULT NULL COMMENT '字段级变更：字段',
  `old_value`     varchar(500) DEFAULT NULL,
  `new_value`     varchar(500) DEFAULT NULL,
  `operator_id`   bigint       DEFAULT NULL,
  `operator_name` varchar(50)  DEFAULT NULL,
  `operate_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流转与变更记录';

-- ---------- 结构化处置记录 ----------
DROP TABLE IF EXISTS `event_disposal`;
CREATE TABLE `event_disposal` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `event_id`      bigint       NOT NULL,
  `actions`       varchar(200) NOT NULL COMMENT '处置动作，多选逗号分隔',
  `targets`       varchar(500) NOT NULL COMMENT '处置对象',
  `exec_device`   varchar(64)  NOT NULL COMMENT '执行设备',
  `exec_time`     datetime     NOT NULL,
  `effect_time`   datetime      DEFAULT NULL,
  `verify_result` varchar(16)  NOT NULL COMMENT 'VERIFIED/PENDING/FAILED',
  `remark`        varchar(1000) DEFAULT NULL,
  `executor_id`   bigint        DEFAULT NULL,
  `executor_name` varchar(50)   DEFAULT NULL,
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`       tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结构化处置记录';

-- ---------- 关联事件 ----------
DROP TABLE IF EXISTS `event_relate`;
CREATE TABLE `event_relate` (
  `id`                bigint NOT NULL AUTO_INCREMENT,
  `event_id`          bigint NOT NULL,
  `related_event_id`  bigint NOT NULL,
  `relate_type`       varchar(16) NOT NULL DEFAULT 'DUPLICATE' COMMENT 'DUPLICATE/SAME_SOURCE',
  `create_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关联事件';

-- ---------- 审计 ----------
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log` (
  `id`          bigint NOT NULL AUTO_INCREMENT,
  `user_id`     bigint DEFAULT NULL,
  `username`    varchar(50) DEFAULT NULL,
  `action`      varchar(64) NOT NULL,
  `object_type` varchar(32) DEFAULT NULL,
  `object_id`   varchar(64) DEFAULT NULL,
  `detail`      text,
  `ip`          varchar(64) DEFAULT NULL,
  `result`      varchar(16) DEFAULT 'OK',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`create_time`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id`         bigint NOT NULL AUTO_INCREMENT,
  `user_id`    bigint DEFAULT NULL,
  `username`   varchar(50) DEFAULT NULL,
  `ip`         varchar(64) DEFAULT NULL,
  `user_agent` varchar(300) DEFAULT NULL,
  `success`    tinyint NOT NULL DEFAULT 1,
  `msg`        varchar(200) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

SET FOREIGN_KEY_CHECKS = 1;
