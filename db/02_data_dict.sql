USE `blued`;
SET NAMES utf8mb4;

DELETE FROM `sys_role`;
INSERT INTO `sys_role` (`id`,`code`,`name`,`remark`) VALUES
 (1,'MONITOR','监测员','值班监测上报'),
 (2,'ANALYST','分析员','研判与复核'),
 (3,'HANDLER','处置员','执行处置'),
 (4,'ADMIN','管理员','平台管理');

DELETE FROM `sys_dict_item`;

-- 攻击类型：大类（parent_id = 0）
INSERT INTO `sys_dict_item` (`id`,`dict_type`,`parent_id`,`code`,`name`,`sort`) VALUES
 (1,'attack_type_category',0,'SCAN','扫描探测类',1),
 (2,'attack_type_category',0,'BRUTE','暴力破解类',2),
 (3,'attack_type_category',0,'WEB','Web 攻击类',3),
 (4,'attack_type_category',0,'EXPLOIT','漏洞利用类',4),
 (5,'attack_type_category',0,'MALWARE','恶意代码类',5),
 (6,'attack_type_category',0,'C2','异常外联类',6),
 (7,'attack_type_category',0,'DOS','拒绝服务类',7),
 (8,'attack_type_category',0,'LATERAL','横向移动类',8),
 (9,'attack_type_category',0,'DATA','数据安全类',9),
 (10,'attack_type_category',0,'OTHER','其他/未知',99);

-- 攻击类型：子类（parent_id 指向大类）
INSERT INTO `sys_dict_item` (`id`,`dict_type`,`parent_id`,`code`,`name`,`sort`) VALUES
 (101,'attack_type',1,'PORT_SCAN','端口扫描',1),
 (102,'attack_type',1,'DIR_SCAN','目录/敏感文件扫描',2),
 (103,'attack_type',1,'VULN_SCAN','漏洞扫描器探测',3),
 (104,'attack_type',1,'ASSET_MAP','资产测绘与指纹识别',4),

 (201,'attack_type',2,'SSH_BRUTE','SSH 爆破',1),
 (202,'attack_type',2,'RDP_BRUTE','RDP 爆破',2),
 (203,'attack_type',2,'WEB_LOGIN_BRUTE','Web 登录爆破',3),
 (204,'attack_type',2,'DB_BRUTE','数据库爆破',4),

 (301,'attack_type',3,'SQLI','SQL 注入',1),
 (302,'attack_type',3,'XSS','XSS',2),
 (303,'attack_type',3,'RCE','命令执行（RCE）',3),
 (304,'attack_type',3,'FILE_UPLOAD','文件上传',4),
 (305,'attack_type',3,'FILE_INCLUDE','文件包含',5),
 (306,'attack_type',3,'SSRF','SSRF',6),
 (307,'attack_type',3,'DESERIALIZE','反序列化',7),
 (308,'attack_type',3,'UNAUTH','越权访问',8),
 (309,'attack_type',3,'PATH_TRAVERSAL','路径遍历',9),

 (401,'attack_type',4,'MIDDLEWARE_EXP','中间件漏洞',1),
 (402,'attack_type',4,'FRAMEWORK_EXP','框架/组件漏洞',2),
 (403,'attack_type',4,'OS_EXP','操作系统漏洞',3),
 (404,'attack_type',4,'APP_EXP','OA/业务系统漏洞',4),

 (501,'attack_type',5,'WEBSHELL_UPLOAD','Webshell 上传',1),
 (502,'attack_type',5,'WEBSHELL_COMM','Webshell 通信',2),
 (503,'attack_type',5,'TROJAN','木马/后门',3),
 (504,'attack_type',5,'MINER','挖矿程序',4),
 (505,'attack_type',5,'RANSOM','勒索软件',5),

 (601,'attack_type',6,'C2_CALLBACK','C2 回连',1),
 (602,'attack_type',6,'DNS_TUNNEL','DNS 隧道',2),
 (603,'attack_type',6,'ABNORMAL_OUT','异常端口外联',3),
 (604,'attack_type',6,'BLACKLINK','暗链/黑页',4),

 (701,'attack_type',7,'DDOS','流量型 DDoS',1),
 (702,'attack_type',7,'CC','CC 攻击',2),
 (703,'attack_type',7,'HIGH_FREQ','异常高频访问',3),

 (801,'attack_type',8,'INTRA_SCAN','内网扫描',1),
 (802,'attack_type',8,'CRED_LATERAL','口令撞库横向',2),
 (803,'attack_type',8,'REMOTE_EXEC','远程执行（PsExec/WMI）',3),
 (804,'attack_type',8,'AD_ATTACK','域内攻击',4),

 (901,'attack_type',9,'DATA_LEAK','数据泄露/拖库',1),
 (902,'attack_type',9,'SENSITIVE_OUT','敏感信息外传',2),
 (903,'attack_type',9,'UNAUTH_DATA','未授权访问',3),

 (1001,'attack_type',10,'UNCATEGORIZED','待分类',1);

-- 其他字典
INSERT INTO `sys_dict_item` (`dict_type`,`parent_id`,`code`,`name`,`sort`) VALUES
 ('attack_phase',0,'RECON','侦察',1),
 ('attack_phase',0,'WEAPONIZE','武器化',2),
 ('attack_phase',0,'DELIVERY','投递',3),
 ('attack_phase',0,'EXPLOIT','利用',4),
 ('attack_phase',0,'INSTALL','安装',5),
 ('attack_phase',0,'C2','命令控制',6),
 ('attack_phase',0,'ACTION','目标达成',7),

 ('attack_direction',0,'OUT2IN','外对内',1),
 ('attack_direction',0,'IN2IN','内对内（横向）',2),
 ('attack_direction',0,'IN2OUT','内对外',3),
 ('attack_direction',0,'UNKNOWN','未知',4),

 ('attack_result',0,'SUCCESS','成功',1),
 ('attack_result',0,'PARTIAL','部分成功',2),
 ('attack_result',0,'FAILED','失败（被拦截）',3),
 ('attack_result',0,'UNKNOWN','未知',4),

 ('event_level',0,'1','高危',1),
 ('event_level',0,'2','中危',2),
 ('event_level',0,'3','低危',3),

 ('event_source',0,'MONITOR','监测发现',1),
 ('event_source',0,'DEVICE','设备告警',2),
 ('event_source',0,'INTEL','情报推送',3),
 ('event_source',0,'MANUAL','人工上报',4),

 ('device_type',0,'WAF','WAF',1),
 ('device_type',0,'IDS','IDS',2),
 ('device_type',0,'IPS','IPS',3),
 ('device_type',0,'HONEYPOT','蜜罐',4),
 ('device_type',0,'SITUATION','态势感知平台',5),
 ('device_type',0,'EDR','EDR',6),
 ('device_type',0,'HOST','主机防护',7),
 ('device_type',0,'NTA','网络流量分析',8),
 ('device_type',0,'OTHER','其他',99),

 ('impact_status',0,'NONE','未影响',1),
 ('impact_status',0,'SUSPECT','疑似影响',2),
 ('impact_status',0,'AFFECTED','已影响',3),
 ('impact_status',0,'COMPROMISED','已失陷',4),

 ('disposal_action',0,'BLOCK_IP','封禁 IP',1),
 ('disposal_action',0,'BLOCK_URL','封禁域名/URL',2),
 ('disposal_action',0,'OFFLINE','下线/隔离系统',3),
 ('disposal_action',0,'CLEAN_WEBSHELL','清理 Webshell',4),
 ('disposal_action',0,'PATCH','修补漏洞',5),
 ('disposal_action',0,'RESET_PWD','重置口令',6),
 ('disposal_action',0,'ADD_POLICY','加装防护策略',7),
 ('disposal_action',0,'DISABLE_ACCOUNT','账号禁用',8),
 ('disposal_action',0,'OTHER','其他',99),

 ('exec_device',0,'FIREWALL','防火墙',1),
 ('exec_device',0,'WAF','WAF',2),
 ('exec_device',0,'IPS','IPS',3),
 ('exec_device',0,'SWITCH','交换机/路由器',4),
 ('exec_device',0,'HOST','主机/服务器',5),
 ('exec_device',0,'CLOUD','云平台',6),
 ('exec_device',0,'OTHER','其他',99),

 ('verify_result',0,'VERIFIED','已验证生效',1),
 ('verify_result',0,'PENDING','待验证',2),
 ('verify_result',0,'FAILED','验证未生效',3),

 ('event_status',0,'SUBMITTED','待受理',1),
 ('event_status',0,'ANALYZING','分析中',2),
 ('event_status',0,'HANDLING','处置中',3),
 ('event_status',0,'REVIEWING','待复核',4),
 ('event_status',0,'ARCHIVED','已归档',5),
 ('event_status',0,'CLOSED','已关闭',6),

 -- 设备名称（管理员可预设，监测员下拉选择）
 ('device_name',0,'WAF-01','WAF-01 主站防护',1),
 ('device_name',0,'WAF-02','WAF-02 业务区防护',2),
 ('device_name',0,'IPS-01','IPS-01 互联网出口',3),
 ('device_name',0,'IDS-01','IDS-01 核心交换镜像',4),
 ('device_name',0,'FW-01','FW-01 边界防火墙',5),
 ('device_name',0,'HONEYPOT-WEB','蜜罐-Web-03',6),
 ('device_name',0,'EDR-SRV','EDR 服务器端',7),
 ('device_name',0,'NTA-01','NTA-01 流量分析',8),
 ('device_name',0,'SOC-PLATFORM','SOC 平台告警',9),
 ('device_name',0,'MANUAL-REPORT','人工/业务反馈',10),

 ('exec_device',0,'MANUAL','人工处置（未接入设备）',9)
;
