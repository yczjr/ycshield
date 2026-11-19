USE `blued`;

-- 攻击源归属地字段（大屏地图用）
ALTER TABLE `event_main`
  ADD COLUMN `attack_region`  varchar(32) DEFAULT NULL COMMENT '攻击源归属地（国内省份 / 境外国家）' AFTER `attack_ips`,
  ADD COLUMN `attack_country` varchar(32) DEFAULT NULL COMMENT '攻击源国家' AFTER `attack_region`;
