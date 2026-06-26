-- =====================================================================
-- V5__fix_portal_seed_encoding.sql
-- 修复 Docker/MySQL 初始化时连接字符集未显式设置导致的门户演示数据乱码。
-- 仅更新 V4 初始化的种子数据，不删除旧表、不影响业务新增数据。
-- =====================================================================

SET NAMES utf8mb4;

UPDATE portal_app_category
SET
  category_name = CASE category_code
    WHEN 'public' THEN '公共系统'
    WHEN 'student_affairs' THEN '学工系统'
    WHEN 'academic' THEN '教务系统'
    WHEN 'dormitory' THEN '宿舍系统'
    WHEN 'research' THEN '教科研管理'
    WHEN 'logistics' THEN '后勤管理'
    WHEN 'asset' THEN '设备资产管理'
    WHEN 'personnel' THEN '人事管理系统'
    WHEN 'system' THEN '系统管理'
    ELSE category_name
  END,
  remark = CASE category_code
    WHEN 'public' THEN '常用公共服务入口'
    WHEN 'student_affairs' THEN '学生事务相关服务'
    WHEN 'academic' THEN '教学、课程、成绩相关'
    WHEN 'dormitory' THEN '住宿相关服务'
    WHEN 'research' THEN '科研与实验相关'
    WHEN 'logistics' THEN '后勤报修等服务'
    WHEN 'asset' THEN '设备与资产管理'
    WHEN 'personnel' THEN '人事相关服务'
    WHEN 'system' THEN '平台后台管理'
    ELSE remark
  END
WHERE category_code IN (
  'public', 'student_affairs', 'academic', 'dormitory', 'research',
  'logistics', 'asset', 'personnel', 'system'
);

UPDATE portal_app SET app_name='实验室预约管理', description='在线预约实验室、查看我的预约与审批进度', icon='calendar', route_path='/lab/reservation', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=1, is_new=1, status=1, sort_order=1 WHERE app_code='LAB_RESERVATION';
UPDATE portal_app SET app_name='应用中心', description='校园服务统一入口', icon='apps', route_path='/portal/apps', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=1, is_new=0, status=1, sort_order=2 WHERE app_code='APP_CENTER';
UPDATE portal_app SET app_name='通知公告', description='查看学校与平台通知公告', icon='notice', route_path='/notices', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=0, is_new=0, status=1, sort_order=3 WHERE app_code='NOTICE_CENTER';
UPDATE portal_app SET app_name='校园资讯', description='校园新闻与动态', icon='news', route_path='/news', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=0, is_new=1, status=1, sort_order=4 WHERE app_code='NEWS_CENTER';
UPDATE portal_app SET app_name='任务中心', description='统一处理待办、已办与我发起的事项', icon='task', route_path='/tasks', external_url='', open_type='internal', visible_roles='teacher,admin', is_hot=0, is_new=0, status=1, sort_order=5 WHERE app_code='TASK_CENTER';
UPDATE portal_app SET app_name='消息中心', description='站内消息与审批结果提醒', icon='message', route_path='/messages', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=0, is_new=0, status=1, sort_order=6 WHERE app_code='MESSAGE_CENTER';
UPDATE portal_app SET app_name='我的日历', description='查看近期预约、审批与日程', icon='calendar', route_path='/calendar', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=0, is_new=0, status=1, sort_order=7 WHERE app_code='CALENDAR';
UPDATE portal_app SET app_name='AI 校园助手', description='智能问答与服务导航', icon='ai', route_path='/ai/assistant', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=1, is_new=1, status=1, sort_order=8 WHERE app_code='AI_ASSISTANT';
UPDATE portal_app SET app_name='管理员工作台', description='管理工作台与数据概览', icon='dashboard', route_path='/admin/dashboard', external_url='', open_type='internal', visible_roles='admin', is_hot=0, is_new=0, status=1, sort_order=9 WHERE app_code='ADMIN_DASHBOARD';
UPDATE portal_app SET app_name='用户管理', description='用户与账号管理', icon='user', route_path='/system/users', external_url='', open_type='internal', visible_roles='admin', is_hot=0, is_new=0, status=1, sort_order=10 WHERE app_code='USER_MANAGE';
UPDATE portal_app SET app_name='应用管理', description='维护应用中心的应用与可见范围', icon='apps', route_path='/admin/apps', external_url='', open_type='internal', visible_roles='admin', is_hot=0, is_new=0, status=1, sort_order=11 WHERE app_code='APP_MANAGE';
UPDATE portal_app SET app_name='课表查询', description='查看我的课程表', icon='book', route_path='/schedule', external_url='', open_type='internal', visible_roles='teacher,student', is_hot=0, is_new=0, status=1, sort_order=20 WHERE app_code='COURSE_QUERY';
UPDATE portal_app SET app_name='成绩查询', description='查询课程成绩（建设中）', icon='chart', route_path='', external_url='', open_type='internal', visible_roles='student', is_hot=0, is_new=0, status=1, sort_order=21 WHERE app_code='SCORE_QUERY';
UPDATE portal_app SET app_name='报修服务', description='后勤报修申请（建设中）', icon='tools', route_path='', external_url='', open_type='internal', visible_roles='admin,teacher,student', is_hot=0, is_new=0, status=1, sort_order=22 WHERE app_code='REPAIR_SERVICE';
UPDATE portal_app SET app_name='图书馆服务', description='图书馆门户（外部链接）', icon='book', route_path='', external_url='https://www.lib.scnu.edu.cn', open_type='external', visible_roles='admin,teacher,student', is_hot=0, is_new=0, status=1, sort_order=23 WHERE app_code='LIBRARY_SERVICE';

UPDATE notice
SET title='实验室预约平台试运行通知',
    content='为提升实验室资源使用效率，校园综合服务平台实验室预约模块现已开放试运行。师生可通过"实验室预约"入口在线提交预约申请，审批通过后即可按时使用，请合理安排使用时间。',
    notice_type='system',
    publish_scope='role',
    target_roles='student,teacher',
    is_top=1,
    status='published',
    publisher_id=1,
    publisher_name='系统管理员'
WHERE notice_no='N-DEMO-0001';

UPDATE notice
SET title='校园综合服务平台上线公告',
    content='校园综合服务平台正式上线，集成应用中心、通知公告、校园资讯、消息中心、任务审批等能力，欢迎广大师生使用并反馈意见。',
    notice_type='system',
    publish_scope='all',
    target_roles=NULL,
    is_top=0,
    status='published',
    publisher_id=1,
    publisher_name='系统管理员'
WHERE notice_no='N-DEMO-0002';

UPDATE news_category
SET category_name = CASE category_code
    WHEN 'campus_news' THEN '校园新闻'
    WHEN 'school_news' THEN '学校动态'
    WHEN 'policy' THEN '政策制度'
    WHEN 'guide' THEN '服务指南'
    WHEN 'sys_notice' THEN '系统公告'
    ELSE category_name
  END
WHERE category_code IN ('campus_news', 'school_news', 'policy', 'guide', 'sys_notice');

UPDATE news
SET title='校园综合服务平台上线试运行',
    summary='一站式校园服务入口正式上线',
    content='学校信息化建设再上新台阶，校园综合服务平台正式上线试运行，师生可通过统一门户访问实验室预约、通知公告、校园资讯等服务，实现"一次登录、多端可用"。',
    category_code='school_news',
    author='信息中心',
    status='published',
    is_top=1,
    publisher_id=1
WHERE news_no='NEWS-DEMO-0001';

UPDATE news
SET title='实验室开放与安全使用指南',
    summary='实验室预约与安全注意事项',
    content='为保障实验教学安全有序，请师生在使用实验室前认真阅读安全须知，按预约时间段进入实验室，爱护设备、注意用电安全，离开时整理好实验台并关闭电源。',
    category_code='guide',
    author='实验中心',
    status='published',
    is_top=0,
    publisher_id=1
WHERE news_no='NEWS-DEMO-0002';

UPDATE news
SET title='关于规范实验室预约审批流程的通知',
    summary='预约审批流程说明',
    content='为提升审批效率，学生提交的实验室预约由教师初审、管理员终审；教师本人申请由管理员直接审批。请相关老师及时处理待办审批任务。',
    category_code='policy',
    author='教务处',
    status='published',
    is_top=0,
    publisher_id=1
WHERE news_no='NEWS-DEMO-0003';

UPDATE calendar_event
SET title='国际课程实验室预约（东A301）', location='东A301', remark='课程实验'
WHERE user_id=4 AND start_time='2026-06-23 09:00:00';

UPDATE calendar_event
SET title='待审批：学生实验室预约', location='线上', remark='请及时处理待办审批'
WHERE user_id=2 AND start_time='2026-06-22 14:00:00';

UPDATE calendar_event
SET title='IBC实验中心授课', location='西A302', remark='教学安排'
WHERE user_id=2 AND start_time='2026-06-24 10:00:00';
