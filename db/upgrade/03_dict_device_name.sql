USE `blued`;
SET NAMES utf8mb4;

-- ============================================================
-- V2 增量：设备名称字典（管理员可预设、监测员下拉选择）
-- ============================================================
DELETE FROM `sys_dict_item` WHERE `dict_type` = 'device_name';
INSERT INTO `sys_dict_item` (`dict_type`,`parent_id`,`code`,`name`,`sort`) VALUES
 ('device_name',0,'WAF-01','WAF-01 主站防护',1),
 ('device_name',0,'WAF-02','WAF-02 业务区防护',2),
 ('device_name',0,'IPS-01','IPS-01 互联网出口',3),
 ('device_name',0,'IDS-01','IDS-01 核心交换镜像',4),
 ('device_name',0,'FW-01','FW-01 边界防火墙',5),
 ('device_name',0,'HONEYPOT-WEB','蜜罐-Web-03',6),
 ('device_name',0,'EDR-SRV','EDR 服务器端',7),
 ('device_name',0,'NTA-01','NTA-01 流量分析',8),
 ('device_name',0,'SOC-PLATFORM','SOC 平台告警',9),
 ('device_name',0,'MANUAL-REPORT','人工/业务反馈',10);

-- 手工处置（不接入设备）执行方式
INSERT INTO `sys_dict_item` (`dict_type`,`parent_id`,`code`,`name`,`sort`) VALUES
 ('exec_device',0,'MANUAL','人工处置（未接入设备）',9)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
