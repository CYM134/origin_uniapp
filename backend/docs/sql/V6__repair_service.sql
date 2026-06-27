-- =====================================================================
-- V6__repair_service.sql
-- 报修服务：师生提交报修，管理员单级审核；同时激活门户中的报修入口。
-- =====================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS repair_requests (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '报修申请主键',
  repair_no VARCHAR(64) NOT NULL COMMENT '报修编号',
  applicant_user_id BIGINT UNSIGNED NOT NULL COMMENT '申请人用户标识',
  applicant_role ENUM('student','teacher') NOT NULL COMMENT '申请人角色',
  applicant_no VARCHAR(64) NOT NULL COMMENT '申请人账号快照',
  applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名快照',
  applicant_phone VARCHAR(20) DEFAULT NULL COMMENT '申请人手机号快照',
  lab_id BIGINT UNSIGNED NOT NULL COMMENT '关联实验室标识',
  lab_name_snapshot VARCHAR(100) NOT NULL COMMENT '实验室名称快照',
  lab_location_snapshot VARCHAR(200) DEFAULT NULL COMMENT '实验室地点快照',
  fault_type VARCHAR(50) NOT NULL COMMENT '故障类型',
  urgency ENUM('normal','urgent','critical') NOT NULL DEFAULT 'normal' COMMENT '紧急程度',
  title VARCHAR(150) NOT NULL COMMENT '报修标题',
  description TEXT NOT NULL COMMENT '故障描述',
  contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  preferred_time VARCHAR(100) DEFAULT NULL COMMENT '期望处理时间',
  status ENUM('pending','approved','rejected','cancelled') NOT NULL DEFAULT 'pending' COMMENT '审核状态',
  admin_review_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '管理员审核人',
  admin_review_name VARCHAR(50) DEFAULT NULL COMMENT '管理员审核人姓名快照',
  admin_review_at DATETIME(3) DEFAULT NULL COMMENT '管理员审核时间',
  admin_comment TEXT COMMENT '管理员审核备注',
  reject_reason TEXT COMMENT '驳回原因',
  submitted_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '提交时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  deleted_at DATETIME(3) DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repair_no (repair_no),
  KEY idx_repair_applicant (applicant_user_id, submitted_at),
  KEY idx_repair_status (status, submitted_at),
  KEY idx_repair_lab (lab_id, submitted_at),
  CONSTRAINT fk_repair_applicant
    FOREIGN KEY (applicant_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_repair_lab
    FOREIGN KEY (lab_id) REFERENCES laboratories (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_repair_admin
    FOREIGN KEY (admin_review_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后勤报修申请表';

UPDATE portal_app
SET app_name='报修服务',
    description='提交实验室设备与环境报修，查看处理进度',
    icon='tools',
    route_path='/repair',
    external_url='',
    open_type='internal',
    visible_roles='admin,teacher,student',
    is_hot=0,
    is_new=1,
    status=1,
    sort_order=22
WHERE app_code='REPAIR_SERVICE';

INSERT INTO repair_requests (
  repair_no, applicant_user_id, applicant_role, applicant_no, applicant_name, applicant_phone,
  lab_id, lab_name_snapshot, lab_location_snapshot, fault_type, urgency, title, description,
  contact_name, contact_phone, preferred_time, status, submitted_at, created_at, updated_at
)
SELECT 'RR-DEMO-0001', 4, 'student', 'S20230001', '王同学', '13900139001',
       1, '国际课程实验室', '综合楼东A301', 'equipment', 'urgent',
       '投影仪无法正常开机', '上课前发现投影仪电源指示灯不亮，尝试更换插座后仍无法开机，请协助检修。',
       '王同学', '13900139001', '今天下午课前', 'pending',
       '2026-06-24 09:15:00.000', '2026-06-24 09:15:00.000', '2026-06-24 09:15:00.000'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM repair_requests WHERE repair_no='RR-DEMO-0001');

INSERT INTO repair_requests (
  repair_no, applicant_user_id, applicant_role, applicant_no, applicant_name, applicant_phone,
  lab_id, lab_name_snapshot, lab_location_snapshot, fault_type, urgency, title, description,
  contact_name, contact_phone, preferred_time, status, admin_review_user_id, admin_review_name,
  admin_review_at, admin_comment, submitted_at, created_at, updated_at
)
SELECT 'RR-DEMO-0002', 2, 'teacher', 'T20230001', '张教授', '13800138001',
       3, '互联网+新商科实验室', '综合楼西A303', 'network', 'normal',
       '部分工位网络不稳定', '第 3 排靠窗侧 4 个工位网络频繁掉线，影响实验课数据同步。',
       '张教授', '13800138001', '本周内', 'approved', 6, '审核管理员',
       '2026-06-24 14:20:00.000', '已安排信息化维护人员本周内处理。',
       '2026-06-23 16:40:00.000', '2026-06-23 16:40:00.000', '2026-06-24 14:20:00.000'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM repair_requests WHERE repair_no='RR-DEMO-0002');

UPDATE repair_requests
SET applicant_name='王同学',
    lab_name_snapshot='国际课程实验室',
    lab_location_snapshot='综合楼东A301',
    fault_type='equipment',
    urgency='urgent',
    title='投影仪无法正常开机',
    description='上课前发现投影仪电源指示灯不亮，尝试更换插座后仍无法开机，请协助检修。',
    contact_name='王同学',
    preferred_time='今天下午课前'
WHERE repair_no='RR-DEMO-0001';

UPDATE repair_requests
SET applicant_name='张教授',
    lab_name_snapshot='互联网+新商科实验室',
    lab_location_snapshot='综合楼西A303',
    fault_type='network',
    urgency='normal',
    title='部分工位网络不稳定',
    description='第 3 排靠窗侧 4 个工位网络频繁掉线，影响实验课数据同步。',
    contact_name='张教授',
    preferred_time='本周内',
    admin_review_name='审核管理员',
    admin_comment='已安排信息化维护人员本周内处理。'
WHERE repair_no='RR-DEMO-0002';
