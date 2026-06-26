-- =====================================================================
-- V4__init_portal_data.sql  门户初始化数据（应用分类/应用/演示公告/资讯/日历）
-- 兼容策略：全部 INSERT IGNORE（依赖唯一键）或 WHERE NOT EXISTS，可重复执行不产生重复数据。
-- 角色编码仅使用现有 admin / teacher / student（admin 在 P0 兼任超级管理员）。
-- 依赖：V1（portal_app*）、V2（notice/news）、V3（calendar）已执行。
-- =====================================================================

SET NAMES utf8mb4;

-- 1) 应用分类 ----------------------------------------------------------
INSERT IGNORE INTO portal_app_category (category_name, category_code, icon, sort_order, status, remark) VALUES
  ('公共系统',     'public',           'apps',     1,  1, '常用公共服务入口'),
  ('学工系统',     'student_affairs',  'student',  2,  1, '学生事务相关服务'),
  ('教务系统',     'academic',         'book',     3,  1, '教学、课程、成绩相关'),
  ('宿舍系统',     'dormitory',        'home',     4,  1, '住宿相关服务'),
  ('教科研管理',   'research',         'flask',    5,  1, '科研与实验相关'),
  ('后勤管理',     'logistics',        'tools',    6,  1, '后勤报修等服务'),
  ('设备资产管理', 'asset',            'box',      7,  1, '设备与资产管理'),
  ('人事管理系统', 'personnel',        'idcard',   8,  1, '人事相关服务'),
  ('系统管理',     'system',           'setting',  99, 1, '平台后台管理');

-- 2) 默认应用 ----------------------------------------------------------
-- 核心应用（均有对应页面或聚合入口）
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '实验室预约管理','LAB_RESERVATION',(SELECT id FROM portal_app_category WHERE category_code='public'),'calendar','在线预约实验室、查看我的预约与审批进度','/lab/reservation','','internal','admin,teacher,student',1,1,1,1;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '应用中心','APP_CENTER',(SELECT id FROM portal_app_category WHERE category_code='public'),'apps','校园服务统一入口','/portal/apps','','internal','admin,teacher,student',1,0,1,2;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '通知公告','NOTICE_CENTER',(SELECT id FROM portal_app_category WHERE category_code='public'),'notice','查看学校与平台通知公告','/notices','','internal','admin,teacher,student',0,0,1,3;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '校园资讯','NEWS_CENTER',(SELECT id FROM portal_app_category WHERE category_code='public'),'news','校园新闻与动态','/news','','internal','admin,teacher,student',0,1,1,4;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '任务中心','TASK_CENTER',(SELECT id FROM portal_app_category WHERE category_code='public'),'task','统一处理待办、已办与我发起的事项','/tasks','','internal','teacher,admin',0,0,1,5;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '消息中心','MESSAGE_CENTER',(SELECT id FROM portal_app_category WHERE category_code='public'),'message','站内消息与审批结果提醒','/messages','','internal','admin,teacher,student',0,0,1,6;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '我的日历','CALENDAR',(SELECT id FROM portal_app_category WHERE category_code='public'),'calendar','查看近期预约、审批与日程','/calendar','','internal','admin,teacher,student',0,0,1,7;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT 'AI 校园助手','AI_ASSISTANT',(SELECT id FROM portal_app_category WHERE category_code='public'),'ai','智能问答与服务导航','/ai/assistant','','internal','admin,teacher,student',1,1,1,8;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '管理员工作台','ADMIN_DASHBOARD',(SELECT id FROM portal_app_category WHERE category_code='system'),'dashboard','管理工作台与数据概览','/admin/dashboard','','internal','admin',0,0,1,9;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '用户管理','USER_MANAGE',(SELECT id FROM portal_app_category WHERE category_code='system'),'user','用户与账号管理','/system/users','','internal','admin',0,0,1,10;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '应用管理','APP_MANAGE',(SELECT id FROM portal_app_category WHERE category_code='system'),'apps','维护应用中心的应用与可见范围','/admin/apps','','internal','admin',0,0,1,11;

-- 教务/学工/后勤等分类的示例应用（部分为"建设中"占位，前端点击给出友好提示）
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '课表查询','COURSE_QUERY',(SELECT id FROM portal_app_category WHERE category_code='academic'),'book','查看我的课程表','/schedule','','internal','teacher,student',0,0,1,20;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '成绩查询','SCORE_QUERY',(SELECT id FROM portal_app_category WHERE category_code='academic'),'chart','查询课程成绩（建设中）','','','internal','student',0,0,1,21;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '报修服务','REPAIR_SERVICE',(SELECT id FROM portal_app_category WHERE category_code='logistics'),'tools','后勤报修申请（建设中）','','','internal','admin,teacher,student',0,0,1,22;
INSERT IGNORE INTO portal_app (app_name, app_code, category_id, icon, description, route_path, external_url, open_type, visible_roles, is_hot, is_new, status, sort_order)
  SELECT '图书馆服务','LIBRARY_SERVICE',(SELECT id FROM portal_app_category WHERE category_code='public'),'book','图书馆门户（外部链接）','','https://www.lib.scnu.edu.cn','external','admin,teacher,student',0,0,1,23;

-- 3) 演示通知公告 ------------------------------------------------------
INSERT IGNORE INTO notice (notice_no, title, content, notice_type, publish_scope, target_roles, is_top, status, publish_time, publisher_id, publisher_name) VALUES
  ('N-DEMO-0001','实验室预约平台试运行通知','为提升实验室资源使用效率，校园综合服务平台实验室预约模块现已开放试运行。师生可通过"实验室预约"入口在线提交预约申请，审批通过后即可按时使用，请合理安排使用时间。','system','role','student,teacher',1,'published',CURRENT_TIMESTAMP(3),1,'系统管理员'),
  ('N-DEMO-0002','校园综合服务平台上线公告','校园综合服务平台正式上线，集成应用中心、通知公告、校园资讯、消息中心、任务审批等能力，欢迎广大师生使用并反馈意见。','system','all',NULL,0,'published',CURRENT_TIMESTAMP(3),1,'系统管理员');

-- 4) 资讯分类与演示资讯 ------------------------------------------------
INSERT IGNORE INTO news_category (category_name, category_code, sort_order, status) VALUES
  ('校园新闻', 'campus_news', 1, 1),
  ('学校动态', 'school_news', 2, 1),
  ('政策制度', 'policy',      3, 1),
  ('服务指南', 'guide',       4, 1),
  ('系统公告', 'sys_notice',  5, 1);

INSERT IGNORE INTO news (news_no, title, summary, content, category_id, category_code, author, status, is_top, publish_time, publisher_id)
  SELECT 'NEWS-DEMO-0001','校园综合服务平台上线试运行','一站式校园服务入口正式上线','学校信息化建设再上新台阶，校园综合服务平台正式上线试运行，师生可通过统一门户访问实验室预约、通知公告、校园资讯等服务，实现"一次登录、多端可用"。',(SELECT id FROM news_category WHERE category_code='school_news'),'school_news','信息中心','published',1,CURRENT_TIMESTAMP(3),1;
INSERT IGNORE INTO news (news_no, title, summary, content, category_id, category_code, author, status, is_top, publish_time, publisher_id)
  SELECT 'NEWS-DEMO-0002','实验室开放与安全使用指南','实验室预约与安全注意事项','为保障实验教学安全有序，请师生在使用实验室前认真阅读安全须知，按预约时间段进入实验室，爱护设备、注意用电安全，离开时整理好实验台并关闭电源。',(SELECT id FROM news_category WHERE category_code='guide'),'guide','实验中心','published',0,CURRENT_TIMESTAMP(3),1;
INSERT IGNORE INTO news (news_no, title, summary, content, category_id, category_code, author, status, is_top, publish_time, publisher_id)
  SELECT 'NEWS-DEMO-0003','关于规范实验室预约审批流程的通知','预约审批流程说明','为提升审批效率，学生提交的实验室预约由教师初审、管理员终审；教师本人申请由管理员直接审批。请相关老师及时处理待办审批任务。',(SELECT id FROM news_category WHERE category_code='policy'),'policy','教务处','published',0,CURRENT_TIMESTAMP(3),1;

-- 5) 演示日历事件（学生 id=4 王同学 / 教师 id=2 张教授）-----------------
INSERT INTO calendar_event (user_id, title, event_type, start_time, end_time, location, remark)
  SELECT 4,'国际课程实验室预约（东A301）','reservation','2026-06-23 09:00:00','2026-06-23 11:00:00','东A301','课程实验'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM calendar_event WHERE user_id=4 AND title='国际课程实验室预约（东A301）' AND start_time='2026-06-23 09:00:00');
INSERT INTO calendar_event (user_id, title, event_type, start_time, end_time, location, remark)
  SELECT 2,'待审批：学生实验室预约','approval','2026-06-22 14:00:00','2026-06-22 14:30:00','线上','请及时处理待办审批'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM calendar_event WHERE user_id=2 AND title='待审批：学生实验室预约' AND start_time='2026-06-22 14:00:00');
INSERT INTO calendar_event (user_id, title, event_type, start_time, end_time, location, remark)
  SELECT 2,'IBC实验中心授课','reservation','2026-06-24 10:00:00','2026-06-24 12:00:00','西A302','教学安排'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM calendar_event WHERE user_id=2 AND title='IBC实验中心授课' AND start_time='2026-06-24 10:00:00');
