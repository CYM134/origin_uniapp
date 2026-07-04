-- ============================================================
-- 校园综合服务平台 数据库全量导出（36 张表：结构 + 数据）
-- 来源库: company_level  导出时间: 2026-07-04
-- 导入方法（本地 MySQL 8.x）:
--   1) CREATE DATABASE company_level DEFAULT CHARACTER SET utf8mb4;
--   2) mysql -uroot -p company_level < company_level_full_dump.sql
--   3) 启动后端时把数据源指向该库（环境变量 DB_URL / DB_USERNAME / DB_PASSWORD，
--      见 backend/src/main/resources/application-dev.yml）
-- ============================================================

-- MySQL dump 10.13  Distrib 9.7.1, for macos26.4 (arm64)
--
-- Host: mysql6.sqlpub.com    Database: company_level
-- ------------------------------------------------------
-- Server version	8.4.3-SQLPub-0.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `academic_semesters`
--

DROP TABLE IF EXISTS `academic_semesters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_semesters` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '学期主键',
  `semester_code` varchar(64) NOT NULL COMMENT '学期编码，例如 2025-2026-1',
  `semester_name` varchar(100) NOT NULL COMMENT '学期显示名称',
  `academic_year` varchar(20) NOT NULL COMMENT '学年，例如 2025-2026',
  `term_no` tinyint unsigned NOT NULL COMMENT '学期序号，通常为 1 或 2',
  `start_date` date DEFAULT NULL COMMENT '学期开始日期',
  `end_date` date DEFAULT NULL COMMENT '学期结束日期',
  `status` enum('active','inactive') NOT NULL DEFAULT 'inactive' COMMENT '学期状态',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_semester_code` (`semester_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学年学期表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `academic_semesters`
--

LOCK TABLES `academic_semesters` WRITE;
/*!40000 ALTER TABLE `academic_semesters` DISABLE KEYS */;
INSERT INTO `academic_semesters` VALUES (1,'2025-2026-2','2025-2026学年第二学期','2025-2026',2,'2026-02-23','2026-07-05','active','2026-01-01 00:00:00.000','2026-06-11 00:00:00.000'),(2,'2026-2027-1','2026-2027学年第一学期','2026-2027',1,'2026-09-01','2027-01-18','inactive','2026-06-01 00:00:00.000','2026-06-01 00:00:00.000');
/*!40000 ALTER TABLE `academic_semesters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_conversation`
--

DROP TABLE IF EXISTS `ai_conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_conversation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户 id',
  `title` varchar(150) DEFAULT NULL COMMENT '会话标题（取首条提问摘要）',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_user_updated` (`user_id`,`updated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 助手会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_conversation`
--

LOCK TABLES `ai_conversation` WRITE;
/*!40000 ALTER TABLE `ai_conversation` DISABLE KEYS */;
INSERT INTO `ai_conversation` VALUES (1,4,'如何预约实验室？','2026-06-21 19:44:18.952','2026-06-21 19:44:19.118'),(2,4,'预约规则','2026-06-23 21:35:20.440','2026-06-23 21:35:28.021'),(3,2,'预约规则','2026-06-23 21:36:28.465','2026-06-23 21:36:28.616'),(4,4,'预约规则','2026-06-26 15:19:56.285','2026-06-26 15:20:26.906'),(5,2,'预约审批流程','2026-06-26 16:01:27.889','2026-06-26 16:01:30.749'),(6,4,'预约审批流程','2026-06-26 16:09:57.073','2026-06-26 16:10:10.046');
/*!40000 ALTER TABLE `ai_conversation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_message`
--

DROP TABLE IF EXISTS `ai_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint unsigned NOT NULL COMMENT '会话 id',
  `role` varchar(20) NOT NULL COMMENT '角色：user/assistant/system',
  `content` mediumtext NOT NULL COMMENT '消息内容',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_conversation_created` (`conversation_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 助手消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_message`
--

LOCK TABLES `ai_message` WRITE;
/*!40000 ALTER TABLE `ai_message` DISABLE KEYS */;
INSERT INTO `ai_message` VALUES (1,1,'user','如何预约实验室？','2026-06-21 19:44:18.970'),(2,1,'assistant','实验室预约流程：在「实验室预约」中选择可用实验室与时间段，填写用途与人数后提交。学生申请先由教师初审、再由管理员终审；教师申请由管理员直接审批。审批结果会在「消息中心」通知您。','2026-06-21 19:44:19.004'),(3,1,'user','预约规则是什么','2026-06-21 19:44:19.069'),(4,1,'assistant','预约规则：同一实验室同一时间段不可重复预约；开始时间必须早于结束时间；停用或维修中的实验室不可预约；取消或被驳回的预约不占用时间段。','2026-06-21 19:44:19.101'),(5,2,'user','预约规则','2026-06-23 21:35:20.486'),(6,2,'assistant','预约规则：同一实验室同一时间段不可重复预约；开始时间必须早于结束时间；停用或维修中的实验室不可预约；取消或被驳回的预约不占用时间段。','2026-06-23 21:35:20.572'),(7,2,'user','预约审批流程','2026-06-23 21:35:23.175'),(8,2,'assistant','实验室预约流程：在「实验室预约」中选择可用实验室与时间段，填写用途与人数后提交。学生申请先由教师初审、再由管理员终审；教师申请由管理员直接审批。审批结果会在「消息中心」通知您。','2026-06-23 21:35:23.256'),(9,2,'user','如何预约实验室','2026-06-23 21:35:24.152'),(10,2,'assistant','实验室预约流程：在「实验室预约」中选择可用实验室与时间段，填写用途与人数后提交。学生申请先由教师初审、再由管理员终审；教师申请由管理员直接审批。审批结果会在「消息中心」通知您。','2026-06-23 21:35:24.247'),(11,2,'user','如何预约实验室','2026-06-23 21:35:26.973'),(12,2,'assistant','实验室预约流程：在「实验室预约」中选择可用实验室与时间段，填写用途与人数后提交。学生申请先由教师初审、再由管理员终审；教师申请由管理员直接审批。审批结果会在「消息中心」通知您。','2026-06-23 21:35:27.054'),(13,2,'user','如何预约实验室','2026-06-23 21:35:27.900'),(14,2,'assistant','实验室预约流程：在「实验室预约」中选择可用实验室与时间段，填写用途与人数后提交。学生申请先由教师初审、再由管理员终审；教师申请由管理员直接审批。审批结果会在「消息中心」通知您。','2026-06-23 21:35:27.981'),(15,3,'user','预约规则','2026-06-23 21:36:28.504'),(16,3,'assistant','预约规则：同一实验室同一时间段不可重复预约；开始时间必须早于结束时间；停用或维修中的实验室不可预约；取消或被驳回的预约不占用时间段。','2026-06-23 21:36:28.578'),(17,4,'user','预约规则','2026-06-26 15:19:56.321'),(18,4,'assistant','您好！预约规则因具体资源（如实验室、会议室、设备等）而异。以下是通用要点：\n\n- **提前预约**：通常需至少提前1个工作日申请，部分热门时段需提前3天。\n- **使用时效**：单次预约时长一般不超过4小时，超时需重新申请。\n- **取消规则**：至少提前2小时取消，否则可能记违规。\n- **诚信管理**：多次违规（如爽约、超时）将暂停预约权限。\n\n如需详细规则（如实验室预约），请提供具体名称，或登录平台查看【预约指南】。','2026-06-26 15:19:59.842'),(19,4,'user','如何预约实验室','2026-06-26 15:20:07.621'),(20,4,'assistant','预约实验室请登录校园服务平台的“实验室预约”模块，选择空闲时段并提交申请，等待管理员审批即可。','2026-06-26 15:20:09.270'),(21,4,'user','预约规则','2026-06-26 15:20:13.828'),(22,4,'assistant','校园预约通常遵循以下核心规则：\n1. **提前预约**：需在开放时段内提前一定时间（如2小时至7天）申请。\n2. **实名认证**：仅限本校师生凭学号/工号登录系统预约。\n3. **时间限制**：每次预约设有最短/最长使用时长（如30分钟至4小时）。\n4. **取消规则**：需在开始前至少30分钟取消，否则可能记违约。\n5. **信用机制**：多次未到或违规将暂停预约权限。\n6. **专属用途**：各场所需按指定功能使用，禁止转借或商用。\n\n如需特定功能区（如实验室、自习室）的详细规则，请说明场所类型。','2026-06-26 15:20:19.752'),(23,4,'user','预约审批流程','2026-06-26 15:20:24.925'),(24,4,'assistant','预约审批流程通常为：提交预约申请 → 导师/管理员初审 → 实验室负责人审批 → 系统通知结果。具体请以平台实际流程为准。','2026-06-26 15:20:26.874'),(25,5,'user','预约审批流程','2026-06-26 16:01:27.928'),(26,5,'assistant','校园预约审批流程通常为：提交预约申请 → 相关管理员或导师审核 → 审批通过/驳回。建议提前1-3个工作日申请，关注系统通知及时查看结果。','2026-06-26 16:01:30.715'),(27,6,'user','预约审批流程','2026-06-26 16:09:57.107'),(28,6,'assistant','预约审批流程通常为：提交预约申请 → 教师/实验室管理员审核 → 通过或驳回。您可以在“我的预约”中查看审批状态，当前您的待审批预约数量为0条。如需了解具体操作，建议查看平台帮助文档。','2026-06-26 16:10:00.015'),(29,6,'user','如何预约实验室','2026-06-26 16:10:08.066'),(30,6,'assistant','预约实验室请通过校园综合服务平台提交申请。如需查看您的预约情况，我可为您查询。','2026-06-26 16:10:10.013');
/*!40000 ALTER TABLE `ai_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '审计日志主键',
  `user_id` bigint unsigned DEFAULT NULL COMMENT '操作人用户标识',
  `role` enum('admin','teacher','student','anonymous') NOT NULL COMMENT '操作人角色',
  `module` varchar(50) NOT NULL COMMENT '业务模块名称',
  `action` varchar(50) NOT NULL COMMENT '动作名称',
  `target_type` varchar(50) DEFAULT NULL COMMENT '目标实体类型',
  `target_id` bigint unsigned DEFAULT NULL COMMENT '目标实体标识',
  `detail` json DEFAULT NULL COMMENT '结构化操作详情',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '客户端网络地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '客户端代理信息',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '日志时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_at`),
  KEY `idx_module_action_created` (`module`,`action`,`created_at`),
  KEY `idx_target` (`target_type`,`target_id`),
  CONSTRAINT `fk_audit_logs_user` FOREIGN KEY (`user_id`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统审计日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,1,'admin','auth','login','sys_users',1,'{\"result\": \"success\"}','127.0.0.1','mock-client','2026-06-11 09:10:00.000'),(2,1,'admin','laboratory','create','laboratories',5,'{\"name\": \"402实验室\", \"labCode\": \"lab5\"}','127.0.0.1','mock-client','2026-01-05 09:20:00.000'),(3,2,'teacher','reservation','approve','reservation_applications',2,'{\"toStatus\": \"teacher_approved\", \"fromStatus\": \"pending\"}','127.0.0.1','mock-client','2026-06-10 16:00:00.000'),(4,6,'admin','reservation','approve','reservation_applications',3,'{\"toStatus\": \"approved\", \"fromStatus\": \"pending\"}','127.0.0.1','mock-client','2026-06-09 15:15:00.000'),(5,1,'admin','schedule','import','schedule_import_batches',1,'{\"status\": \"success\", \"successCount\": 128}','127.0.0.1','mock-client','2026-06-01 09:02:35.000'),(6,4,'student','reservation','submit','reservation_applications',6,NULL,NULL,'backend-api','2026-06-14 13:15:15.387'),(7,2,'teacher','reservation','approve','reservation_applications',6,NULL,NULL,'backend-api','2026-06-14 13:15:15.757'),(8,1,'admin','reservation','approve','reservation_applications',6,NULL,NULL,'backend-api','2026-06-14 13:15:15.992'),(9,4,'student','reservation','complete','reservation_applications',6,NULL,NULL,'backend-api','2026-06-14 13:15:16.283'),(10,2,'teacher','reservation','submit','reservation_applications',7,NULL,NULL,'backend-api','2026-06-14 13:15:16.539'),(11,1,'admin','reservation','approve','reservation_applications',7,NULL,NULL,'backend-api','2026-06-14 13:15:16.793'),(12,1,'admin','laboratory','create','laboratories',6,'{\"name\": \"测试实验室\"}',NULL,'backend-api','2026-06-14 13:15:16.958'),(13,1,'admin','laboratory','update','laboratories',6,'{\"name\": \"改名实验室\"}',NULL,'backend-api','2026-06-14 13:15:17.113'),(14,1,'admin','laboratory','delete','laboratories',6,NULL,NULL,'backend-api','2026-06-14 13:15:17.233'),(15,1,'admin','user','create','sys_users',7,NULL,NULL,'backend-api','2026-06-14 13:31:41.151'),(16,1,'admin','schedule','import','schedule_import_batches',3,NULL,NULL,'backend-api','2026-06-14 13:31:41.470'),(17,1,'admin','schedule','export','schedule_export_tasks',2,NULL,NULL,'backend-api','2026-06-14 13:31:41.541'),(18,1,'admin','teacher_registration','reject','teacher_registration_applications',1,NULL,NULL,'backend-api','2026-06-14 13:31:41.677'),(19,4,'student','auth','change_password','sys_users',4,NULL,NULL,'backend-api','2026-06-14 13:31:42.168'),(20,8,'student','auth','register','sys_users',8,NULL,NULL,'backend-api','2026-06-14 13:32:38.380'),(21,NULL,'anonymous','auth','teacher_register_apply','teacher_registration_applications',4,NULL,NULL,'backend-api','2026-06-14 13:32:38.703'),(22,1,'admin','user','create','sys_users',9,NULL,NULL,'backend-api','2026-06-14 13:32:55.162'),(23,1,'admin','user','toggle_status','sys_users',9,NULL,NULL,'backend-api','2026-06-14 13:32:55.265'),(24,1,'admin','user','reset_password','sys_users',9,NULL,NULL,'backend-api','2026-06-14 13:32:55.409'),(25,1,'admin','user','update','sys_users',9,NULL,NULL,'backend-api','2026-06-14 13:32:55.473'),(26,1,'admin','user','delete','sys_users',9,NULL,NULL,'backend-api','2026-06-14 13:32:55.554'),(27,4,'student','reservation','submit','reservation_applications',8,NULL,NULL,'backend-api','2026-06-14 13:44:43.039'),(28,1,'admin','reservation','approve','reservation_applications',8,NULL,NULL,'backend-api','2026-06-14 13:44:43.267'),(29,1,'admin','laboratory','create','laboratories',7,'{\"name\": \"测试新增\"}',NULL,'backend-api','2026-06-16 13:42:39.307'),(30,1,'admin','schedule','import','schedule_import_batches',4,NULL,NULL,'backend-api','2026-06-16 13:43:17.722'),(31,1,'admin','schedule','export','schedule_export_tasks',3,NULL,NULL,'backend-api','2026-06-16 13:43:25.769'),(32,2,'teacher','reservation','reject','reservation_applications',1,NULL,NULL,'backend-api','2026-06-16 13:56:17.414'),(33,4,'student','reservation','submit','reservation_applications',9,NULL,NULL,'backend-api','2026-06-16 15:16:58.453'),(34,2,'teacher','reservation','approve','reservation_applications',9,NULL,NULL,'backend-api','2026-06-16 15:17:37.514'),(35,1,'admin','reservation','approve','reservation_applications',9,NULL,NULL,'backend-api','2026-06-16 15:18:28.417'),(36,1,'admin','teacher_registration','approve','teacher_registration_applications',4,NULL,NULL,'backend-api','2026-06-16 15:19:37.629'),(37,1,'admin','reservation','approve','reservation_applications',2,NULL,NULL,'backend-api','2026-06-16 16:38:42.747'),(38,4,'student','reservation','submit','reservation_applications',10,NULL,NULL,'backend-api','2026-06-16 16:40:27.863'),(39,2,'teacher','reservation','approve','reservation_applications',10,NULL,NULL,'backend-api','2026-06-16 16:41:17.330'),(40,1,'admin','reservation','reject','reservation_applications',10,NULL,NULL,'backend-api','2026-06-16 16:41:58.861'),(41,1,'admin','file','create_upload_token','file_objects',1,NULL,NULL,'backend-api','2026-06-21 02:57:04.926'),(42,1,'admin','file','confirm_upload','file_objects',1,NULL,NULL,'backend-api','2026-06-21 02:57:07.671'),(43,1,'admin','schedule','import','schedule_import_batches',5,NULL,NULL,'backend-api','2026-06-21 18:06:49.990'),(44,1,'admin','schedule','export','schedule_export_tasks',4,NULL,NULL,'backend-api','2026-06-21 18:06:59.891'),(45,1,'admin','notice','create','notice',5,NULL,NULL,'backend-api','2026-06-21 19:44:17.983'),(46,1,'admin','news','create','news',7,NULL,NULL,'backend-api','2026-06-21 19:44:18.301'),(47,4,'student','reservation','submit','reservation_applications',11,NULL,NULL,'backend-api','2026-06-21 19:44:19.368'),(48,4,'student','reservation','submit','reservation_applications',12,NULL,NULL,'backend-api','2026-06-22 22:20:33.314'),(49,2,'teacher','reservation','reject','reservation_applications',12,NULL,NULL,'backend-api','2026-06-22 22:21:53.046'),(50,2,'teacher','reservation','approve','reservation_applications',11,NULL,NULL,'backend-api','2026-06-22 22:21:57.760'),(51,1,'admin','reservation','approve','reservation_applications',11,NULL,NULL,'backend-api','2026-06-22 22:22:33.251');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `calendar_event`
--

DROP TABLE IF EXISTS `calendar_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calendar_event` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户 id（个人日程）',
  `title` varchar(150) NOT NULL COMMENT '事件标题',
  `event_type` varchar(30) NOT NULL DEFAULT 'custom' COMMENT '类型：reservation/approval/school/custom',
  `start_time` datetime(3) NOT NULL COMMENT '开始时间',
  `end_time` datetime(3) DEFAULT NULL COMMENT '结束时间',
  `location` varchar(150) DEFAULT NULL COMMENT '地点',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `related_business_type` varchar(50) DEFAULT NULL COMMENT '关联业务类型，如 reservation',
  `related_business_id` bigint unsigned DEFAULT NULL COMMENT '关联业务 id',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_user_start` (`user_id`,`start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日历事件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calendar_event`
--

LOCK TABLES `calendar_event` WRITE;
/*!40000 ALTER TABLE `calendar_event` DISABLE KEYS */;
INSERT INTO `calendar_event` VALUES (1,4,'国际课程实验室预约（东A301）','reservation','2026-06-23 09:00:00.000','2026-06-23 11:00:00.000','东A301','课程实验',NULL,NULL,'2026-06-21 19:25:23.675','2026-06-21 19:25:23.675'),(2,2,'待审批：学生实验室预约','approval','2026-06-22 14:00:00.000','2026-06-22 14:30:00.000','线上','请及时处理待办审批',NULL,NULL,'2026-06-21 19:25:23.707','2026-06-21 19:25:23.707'),(3,2,'IBC实验中心授课','reservation','2026-06-24 10:00:00.000','2026-06-24 12:00:00.000','西A302','教学安排',NULL,NULL,'2026-06-21 19:25:23.739','2026-06-21 19:25:23.739');
/*!40000 ALTER TABLE `calendar_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campus_resources`
--

DROP TABLE IF EXISTS `campus_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campus_resources` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '资源主键',
  `resource_code` varchar(80) NOT NULL COMMENT '资源编码',
  `resource_type` varchar(40) NOT NULL COMMENT '资源类型：lab/meeting_room/study_room/equipment/venue',
  `name` varchar(120) NOT NULL COMMENT '资源名称',
  `location` varchar(200) DEFAULT NULL COMMENT '资源位置',
  `capacity` int unsigned NOT NULL DEFAULT '0' COMMENT '容量或可容纳人数',
  `equipment` text COMMENT '设备或资源说明',
  `image_url` varchar(500) DEFAULT NULL COMMENT '资源图片',
  `reservable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可预约',
  `status` enum('active','maintenance','disabled','deleted') NOT NULL DEFAULT 'active' COMMENT '资源状态',
  `source_table` varchar(80) DEFAULT NULL COMMENT '来源业务表',
  `source_id` bigint unsigned DEFAULT NULL COMMENT '来源业务表主键',
  `created_by` bigint unsigned DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint unsigned DEFAULT NULL COMMENT '更新人',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`),
  UNIQUE KEY `uk_resource_source` (`source_table`,`source_id`),
  KEY `idx_type_status_reservable` (`resource_type`,`status`,`reservable`),
  KEY `idx_location` (`location`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_updated_by` (`updated_by`),
  CONSTRAINT `fk_campus_resources_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_campus_resources_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园统一可预约资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campus_resources`
--

LOCK TABLES `campus_resources` WRITE;
/*!40000 ALTER TABLE `campus_resources` DISABLE KEYS */;
INSERT INTO `campus_resources` VALUES (1,'lab-1','lab','国际课程实验室','综合楼东A301',65,'可移动组合桌椅60套，纳米投影书写墙，话筒2支，服务器2台','/static/images/东A301.png',1,'active','laboratories',1,1,1,'2026-01-05 09:00:00.000','2026-06-01 09:00:00.000',NULL),(2,'lab-2','lab','IBC实验中心','综合楼西A302',100,'开放公共自习室，可容纳百人，打印机、投影仪等设施齐全','/static/images/西A302.jpg',1,'active','laboratories',2,1,1,'2026-01-05 09:05:00.000','2026-06-01 09:05:00.000',NULL),(3,'lab-3','lab','互联网+新商科实验室','综合楼西A303',80,'80个智能工位，配备翻盖式电脑，seewo 2台','/static/images/西A303.png',1,'active','laboratories',3,1,1,'2026-01-05 09:10:00.000','2026-06-01 09:10:00.000',NULL),(4,'lab-4','lab','法语实验室','综合楼西A305',60,'语音教学设备、录播系统、投影仪','/static/images/天空实验室.png',0,'maintenance','laboratories',4,1,6,'2026-01-05 09:15:00.000','2026-06-10 16:00:00.000',NULL),(5,'lab-5','lab','402实验室','综合楼西A402',80,'开发环境、测试工具、标准配置电脑','/static/images/天空实验室.png',1,'active','laboratories',5,1,1,'2026-01-05 09:20:00.000','2026-06-01 09:20:00.000',NULL),(6,'lab-6','lab','改名实验室','测试楼102',40,'x',NULL,0,'deleted','laboratories',6,1,1,'2026-06-14 13:15:16.931','2026-06-14 13:15:17.213','2026-06-14 13:15:17.213'),(7,'lab-7','lab','测试新增','测试',0,'测试','blob:http://localhost:5173/98a9fec4-abdb-45d3-897d-161d171ea3e5',1,'active','laboratories',7,1,1,'2026-06-16 13:42:39.236','2026-06-16 13:42:39.236',NULL);
/*!40000 ALTER TABLE `campus_resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_schedules`
--

DROP TABLE IF EXISTS `course_schedules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_schedules` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '课程课表主键',
  `schedule_no` varchar(64) NOT NULL COMMENT '课表业务编号',
  `semester_id` bigint unsigned NOT NULL COMMENT '关联学期表主键',
  `lab_id` bigint unsigned NOT NULL COMMENT '关联实验室表主键',
  `teacher_user_id` bigint unsigned DEFAULT NULL COMMENT '关联教师用户表主键',
  `teacher_name_snapshot` varchar(50) DEFAULT NULL COMMENT '教师姓名快照',
  `course_name` varchar(150) NOT NULL COMMENT '课程名称',
  `course_type` varchar(50) DEFAULT NULL COMMENT '课程类型',
  `schedule_date` date NOT NULL COMMENT '上课日期',
  `weekday` tinyint unsigned DEFAULT NULL COMMENT '星期序号，1 到 7',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `time_slot_label` varchar(50) DEFAULT NULL COMMENT '显示用时间段标签',
  `planned_student_count` int unsigned NOT NULL DEFAULT '0' COMMENT '计划学生人数',
  `current_student_count` int unsigned NOT NULL DEFAULT '0' COMMENT '当前学生人数',
  `max_student_count` int unsigned NOT NULL DEFAULT '0' COMMENT '最大学生人数',
  `status` enum('scheduled','available','full','ongoing','cancelled') NOT NULL DEFAULT 'scheduled' COMMENT '课表状态',
  `can_reserve` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许基于该课表预约',
  `description` text COMMENT '课程说明',
  `remark` text COMMENT '备注',
  `source_batch_id` bigint unsigned DEFAULT NULL COMMENT '来源导入批次标识',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_no` (`schedule_no`),
  KEY `idx_date_lab` (`schedule_date`,`lab_id`),
  KEY `idx_teacher_date` (`teacher_user_id`,`schedule_date`),
  KEY `idx_semester_date` (`semester_id`,`schedule_date`),
  KEY `idx_source_batch_id` (`source_batch_id`),
  KEY `fk_course_schedules_lab` (`lab_id`),
  CONSTRAINT `fk_course_schedules_import_batch` FOREIGN KEY (`source_batch_id`) REFERENCES `schedule_import_batches` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_course_schedules_lab` FOREIGN KEY (`lab_id`) REFERENCES `laboratories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_course_schedules_semester` FOREIGN KEY (`semester_id`) REFERENCES `academic_semesters` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_course_schedules_teacher` FOREIGN KEY (`teacher_user_id`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_course_schedules_time` CHECK ((`end_time` > `start_time`)),
  CONSTRAINT `chk_course_schedules_weekday` CHECK (((`weekday` is null) or (`weekday` between 1 and 7)))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实验室课程课表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_schedules`
--

LOCK TABLES `course_schedules` WRITE;
/*!40000 ALTER TABLE `course_schedules` DISABLE KEYS */;
INSERT INTO `course_schedules` VALUES (1,'CS20260611001',1,3,2,'张教授','数据结构与算法','理论课','2026-06-12',5,'08:00:00','09:50:00','08:00-09:50',30,25,30,'available',1,'学习基本的数据结构和算法设计方法。','导入课表样例',1,'2026-06-01 09:05:00.000','2026-06-01 09:05:00.000',NULL),(2,'CS20260611002',1,1,3,'李老师','计算机网络','实验课','2026-06-12',5,'10:00:00','11:50:00','10:00-11:50',30,30,30,'full',0,'计算机网络协议和网络安全实验。','人数已满',1,'2026-06-01 09:05:30.000','2026-06-01 09:05:30.000',NULL),(3,'CS20260611003',1,4,2,'张教授','软件工程','实践课','2026-06-12',5,'14:00:00','15:50:00','14:00-15:50',25,20,25,'ongoing',0,'软件开发生命周期实践。','维修期间仅供查看',1,'2026-06-01 09:06:00.000','2026-06-10 16:00:00.000',NULL),(4,'CS20260611004',1,5,3,'李老师','数据库系统','实验课','2026-06-13',6,'19:00:00','20:50:00','19:00-20:50',28,18,28,'scheduled',1,'数据库建模与查询优化实验。',NULL,1,'2026-06-01 09:06:30.000','2026-06-01 09:06:30.000',NULL);
/*!40000 ALTER TABLE `course_schedules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_backup_records`
--

DROP TABLE IF EXISTS `data_backup_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `data_backup_records` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '数据备份记录主键',
  `backup_no` varchar(64) NOT NULL COMMENT '备份业务编号',
  `backup_type` enum('labs','schedules','reservations','users','all') NOT NULL COMMENT '备份数据类型',
  `backup_name` varchar(150) NOT NULL COMMENT '备份显示名称',
  `file_url` varchar(500) DEFAULT NULL COMMENT '备份文件地址',
  `file_size_bytes` bigint unsigned DEFAULT NULL COMMENT '备份文件大小，单位字节',
  `display_size` varchar(50) DEFAULT NULL COMMENT '显示用文件大小文本',
  `status` enum('processing','success','failed','deleted') NOT NULL DEFAULT 'processing' COMMENT '备份状态',
  `progress` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '备份进度百分比',
  `created_by` bigint unsigned NOT NULL COMMENT '创建备份的管理员用户标识',
  `started_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '备份开始时间',
  `completed_at` datetime(3) DEFAULT NULL COMMENT '备份完成时间',
  `restored_at` datetime(3) DEFAULT NULL COMMENT '最近恢复时间',
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_backup_no` (`backup_no`),
  KEY `idx_type_status` (`backup_type`,`status`),
  KEY `idx_started_at` (`started_at`),
  KEY `idx_created_by` (`created_by`),
  CONSTRAINT `fk_data_backup_records_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_data_backup_progress` CHECK ((`progress` <= 100))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据备份记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_backup_records`
--

LOCK TABLES `data_backup_records` WRITE;
/*!40000 ALTER TABLE `data_backup_records` DISABLE KEYS */;
INSERT INTO `data_backup_records` VALUES (1,'BKP20260611001','all','全部数据备份','/backups/all-20260611.sql.gz',5452595,'5.2MB','success',100,1,'2026-06-11 02:00:00.000','2026-06-11 02:01:30.000',NULL,NULL),(2,'BKP20260610001','labs','实验室数据备份','/backups/labs-20260610.sql.gz',1887436,'1.8MB','success',100,1,'2026-06-10 02:00:00.000','2026-06-10 02:00:40.000',NULL,NULL),(3,'BKP20260609001','schedules','课表数据备份','/backups/schedules-20260609.sql.gz',2411725,'2.3MB','success',100,1,'2026-06-09 02:00:00.000','2026-06-09 02:00:50.000',NULL,NULL),(4,'BKP20260614133140389','labs','实验室数据备份',NULL,NULL,'5.2MB','success',100,1,'2026-06-14 13:31:40.907','2026-06-14 13:31:40.907',NULL,NULL),(5,'BKP20260614133140118','all','全部数据备份',NULL,NULL,'1.2MB','success',100,1,'2026-06-14 13:31:40.963','2026-06-14 13:31:40.963',NULL,NULL);
/*!40000 ALTER TABLE `data_backup_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_biz_relations`
--

DROP TABLE IF EXISTS `file_biz_relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_biz_relations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'relation primary key',
  `file_id` bigint unsigned NOT NULL COMMENT 'file primary key',
  `biz_type` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'business type: reservation/resource/activity/profile',
  `biz_id` bigint unsigned DEFAULT NULL COMMENT 'business object id',
  `relation_type` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'attachment' COMMENT 'relation type: attachment/avatar/icon/image',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_biz_relation` (`file_id`,`biz_type`,`biz_id`,`relation_type`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  CONSTRAINT `fk_file_biz_relations_file` FOREIGN KEY (`file_id`) REFERENCES `file_objects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='file business relation';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_biz_relations`
--

LOCK TABLES `file_biz_relations` WRITE;
/*!40000 ALTER TABLE `file_biz_relations` DISABLE KEYS */;
INSERT INTO `file_biz_relations` VALUES (1,1,'reservation',1,'attachment','2026-06-21 02:57:04.887');
/*!40000 ALTER TABLE `file_biz_relations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_objects`
--

DROP TABLE IF EXISTS `file_objects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_objects` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'file primary key',
  `file_no` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'file number',
  `storage_provider` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'minio' COMMENT 'storage provider: minio/oss/local',
  `bucket_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'campus-files' COMMENT 'bucket name',
  `object_key` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'object storage key',
  `original_file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'original file name',
  `file_ext` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'file extension',
  `content_type` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'MIME type',
  `file_size` bigint unsigned NOT NULL DEFAULT '0' COMMENT 'file size in bytes',
  `checksum_sha256` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SHA-256 checksum',
  `visibility` enum('private','public') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'private' COMMENT 'visibility',
  `upload_status` enum('pending','uploaded','deleted') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'upload status',
  `uploader_user_id` bigint unsigned DEFAULT NULL COMMENT 'uploader user id',
  `uploader_role` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'uploader role',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted_at` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_no` (`file_no`),
  UNIQUE KEY `uk_object_key` (`object_key`),
  KEY `idx_uploader_status` (`uploader_user_id`,`upload_status`),
  KEY `idx_status_created` (`upload_status`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='file object metadata';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_objects`
--

LOCK TABLES `file_objects` WRITE;
/*!40000 ALTER TABLE `file_objects` DISABLE KEYS */;
INSERT INTO `file_objects` VALUES (1,'FILE20260621025705056740098','minio','campus-files','reservation/2026/06/21/16ba3f61-4e45-4e5e-9037-f51273ea73ec.txt','test-upload.txt','txt','text/plain',17,NULL,'private','uploaded',1,'admin','2026-06-21 02:57:04.850','2026-06-21 02:57:07.603',NULL);
/*!40000 ALTER TABLE `file_objects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `laboratories`
--

DROP TABLE IF EXISTS `laboratories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `laboratories` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '实验室主键',
  `lab_code` varchar(64) NOT NULL COMMENT '实验室业务编码',
  `name` varchar(100) NOT NULL COMMENT '实验室名称',
  `location` varchar(200) NOT NULL COMMENT '实验室位置',
  `capacity` int unsigned NOT NULL DEFAULT '0' COMMENT '最大容量',
  `equipment` text COMMENT '设备说明',
  `image_url` varchar(500) DEFAULT NULL COMMENT '预览图片地址',
  `status` enum('active','maintenance','disabled','deleted') NOT NULL DEFAULT 'active' COMMENT '实验室状态',
  `created_by` bigint unsigned DEFAULT NULL COMMENT '创建人管理员用户标识',
  `updated_by` bigint unsigned DEFAULT NULL COMMENT '最后更新人管理员用户标识',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_code` (`lab_code`),
  KEY `idx_status` (`status`),
  KEY `idx_name` (`name`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_updated_by` (`updated_by`),
  CONSTRAINT `fk_laboratories_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_laboratories_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实验室基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `laboratories`
--

LOCK TABLES `laboratories` WRITE;
/*!40000 ALTER TABLE `laboratories` DISABLE KEYS */;
INSERT INTO `laboratories` VALUES (1,'lab1','国际课程实验室','综合楼东A301',65,'可移动组合桌椅60套，纳米投影书写墙，话筒2支，服务器2台','/static/images/东A301.png','active',1,1,'2026-01-05 09:00:00.000','2026-06-01 09:00:00.000',NULL),(2,'lab2','IBC实验中心','综合楼西A302',100,'开放公共自习室，可容纳百人，打印机、投影仪等设施齐全','/static/images/西A302.jpg','active',1,1,'2026-01-05 09:05:00.000','2026-06-01 09:05:00.000',NULL),(3,'lab3','互联网+新商科实验室','综合楼西A303',80,'80个智能工位，配备翻盖式电脑，seewo 2台','/static/images/西A303.png','active',1,1,'2026-01-05 09:10:00.000','2026-06-01 09:10:00.000',NULL),(4,'lab4','法语实验室','综合楼西A305',60,'语音教学设备、录播系统、投影仪','/static/images/天空实验室.png','maintenance',1,6,'2026-01-05 09:15:00.000','2026-06-10 16:00:00.000',NULL),(5,'lab5','402实验室','综合楼西A402',80,'开发环境、测试工具、标准配置电脑','/static/images/天空实验室.png','active',1,1,'2026-01-05 09:20:00.000','2026-06-01 09:20:00.000',NULL),(6,'LAB20260614131516422','改名实验室','测试楼102',40,'x',NULL,'deleted',1,1,'2026-06-14 13:15:16.931','2026-06-14 13:15:17.213','2026-06-14 13:15:17.213'),(7,'LAB20260616134239316673606','测试新增','测试',0,'测试','blob:http://localhost:5173/98a9fec4-abdb-45d3-897d-161d171ea3e5','active',1,1,'2026-06-16 13:42:39.236','2026-06-16 13:42:39.236',NULL);
/*!40000 ALTER TABLE `laboratories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `news`
--

DROP TABLE IF EXISTS `news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `news_no` varchar(64) NOT NULL COMMENT '资讯业务编号，唯一',
  `title` varchar(150) NOT NULL COMMENT '标题',
  `summary` varchar(300) DEFAULT NULL COMMENT '摘要',
  `content` mediumtext COMMENT '正文',
  `category_id` bigint unsigned DEFAULT NULL COMMENT '资讯分类 id',
  `category_code` varchar(50) DEFAULT NULL COMMENT '资讯分类编码快照',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `author` varchar(50) DEFAULT NULL COMMENT '作者/来源',
  `status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft 草稿，published 已发布，offline 已下线',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶',
  `publish_time` datetime(3) DEFAULT NULL COMMENT '发布时间',
  `publisher_id` bigint unsigned DEFAULT NULL COMMENT '发布人 id',
  `view_count` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '逻辑删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_no` (`news_no`),
  KEY `idx_status_publish` (`status`,`publish_time`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园资讯';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `news`
--

LOCK TABLES `news` WRITE;
/*!40000 ALTER TABLE `news` DISABLE KEYS */;
INSERT INTO `news` VALUES (1,'NEWS-DEMO-0001','校园综合服务平台上线试运行','一站式校园服务入口正式上线','学校信息化建设再上新台阶，校园综合服务平台正式上线试运行，师生可通过统一门户访问实验室预约、通知公告、校园资讯等服务，实现\"一次登录、多端可用\"。',2,'school_news',NULL,'信息中心','published',1,'2026-06-21 19:25:23.579',1,2,'2026-06-21 19:25:23.579','2026-06-26 15:33:25.221',NULL),(2,'NEWS-DEMO-0002','实验室开放与安全使用指南','实验室预约与安全注意事项','为保障实验教学安全有序，请师生在使用实验室前认真阅读安全须知，按预约时间段进入实验室，爱护设备、注意用电安全，离开时整理好实验台并关闭电源。',4,'guide',NULL,'实验中心','published',0,'2026-06-21 19:25:23.612',1,0,'2026-06-21 19:25:23.612','2026-06-21 19:25:23.612',NULL),(3,'NEWS-DEMO-0003','关于规范实验室预约审批流程的通知','预约审批流程说明','为提升审批效率，学生提交的实验室预约由教师初审、管理员终审；教师本人申请由管理员直接审批。请相关老师及时处理待办审批任务。',3,'policy',NULL,'教务处','published',0,'2026-06-21 19:25:23.643',1,0,'2026-06-21 19:25:23.643','2026-06-21 19:25:23.643',NULL),(7,'NW20260621194418157757779','集成测试资讯-自动','摘要','正文',NULL,NULL,NULL,NULL,'published',0,'2026-06-21 19:44:18.287',1,0,'2026-06-21 19:44:18.287','2026-06-21 19:44:58.770','2026-06-21 19:44:58.770');
/*!40000 ALTER TABLE `news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `news_category`
--

DROP TABLE IF EXISTS `news_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news_category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) NOT NULL COMMENT '分类编码，唯一',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1 启用，0 停用',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_category_code` (`category_code`),
  KEY `idx_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园资讯分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `news_category`
--

LOCK TABLES `news_category` WRITE;
/*!40000 ALTER TABLE `news_category` DISABLE KEYS */;
INSERT INTO `news_category` VALUES (1,'校园新闻','campus_news',1,1,'2026-06-21 19:25:23.546','2026-06-21 19:25:23.546'),(2,'学校动态','school_news',2,1,'2026-06-21 19:25:23.546','2026-06-21 19:25:23.546'),(3,'政策制度','policy',3,1,'2026-06-21 19:25:23.546','2026-06-21 19:25:23.546'),(4,'服务指南','guide',4,1,'2026-06-21 19:25:23.546','2026-06-21 19:25:23.546'),(5,'系统公告','sys_notice',5,1,'2026-06-21 19:25:23.546','2026-06-21 19:25:23.546');
/*!40000 ALTER TABLE `news_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `notice_no` varchar(64) NOT NULL COMMENT '公告业务编号，唯一',
  `title` varchar(150) NOT NULL COMMENT '标题',
  `content` mediumtext NOT NULL COMMENT '正文',
  `notice_type` varchar(30) NOT NULL DEFAULT 'system' COMMENT '类型：system/activity/maintenance/policy 等',
  `publish_scope` varchar(20) NOT NULL DEFAULT 'all' COMMENT '发布范围：all 全体，role 指定角色',
  `target_roles` varchar(100) DEFAULT NULL COMMENT '目标角色编码，逗号分隔（scope=role 时生效）',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶',
  `status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft 草稿，published 已发布，offline 已下线',
  `publish_time` datetime(3) DEFAULT NULL COMMENT '发布时间',
  `publisher_id` bigint unsigned DEFAULT NULL COMMENT '发布人 id',
  `publisher_name` varchar(50) DEFAULT NULL COMMENT '发布人姓名快照',
  `view_count` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '逻辑删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_no` (`notice_no`),
  KEY `idx_status_publish` (`status`,`publish_time`),
  KEY `idx_top_publish` (`is_top`,`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (1,'N-DEMO-0001','实验室预约平台试运行通知','为提升实验室资源使用效率，校园综合服务平台实验室预约模块现已开放试运行。师生可通过\"实验室预约\"入口在线提交预约申请，审批通过后即可按时使用，请合理安排使用时间。','system','role','student,teacher',1,'published','2026-06-21 19:25:23.514',1,'系统管理员',2,'2026-06-21 19:25:23.514','2026-06-23 21:36:05.067',NULL),(2,'N-DEMO-0002','校园综合服务平台上线公告','校园综合服务平台正式上线，集成应用中心、通知公告、校园资讯、消息中心、任务审批等能力，欢迎广大师生使用并反馈意见。','system','all',NULL,0,'published','2026-06-21 19:25:23.514',1,'系统管理员',1,'2026-06-21 19:25:23.514','2026-06-23 21:36:01.332',NULL),(5,'NT20260621194417826363862','集成测试公告-自动','这是集成测试创建的公告内容。','system','all',NULL,0,'published','2026-06-21 19:44:17.958',1,'系统管理员',0,'2026-06-21 19:44:17.958','2026-06-21 19:44:58.737','2026-06-21 19:44:58.737');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice_read`
--

DROP TABLE IF EXISTS `notice_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice_read` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `notice_id` bigint unsigned NOT NULL COMMENT '公告 id',
  `user_id` bigint unsigned NOT NULL COMMENT '用户 id',
  `read_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`,`user_id`),
  KEY `idx_user_time` (`user_id`,`read_time`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告阅读记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice_read`
--

LOCK TABLES `notice_read` WRITE;
/*!40000 ALTER TABLE `notice_read` DISABLE KEYS */;
INSERT INTO `notice_read` VALUES (1,1,4,'2026-06-21 19:44:17.810'),(2,2,2,'2026-06-23 21:36:01.300'),(3,1,2,'2026-06-23 21:36:05.037');
/*!40000 ALTER TABLE `notice_read` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '通知主键',
  `notification_no` varchar(64) NOT NULL COMMENT '通知业务编号',
  `recipient_user_id` bigint unsigned NOT NULL COMMENT '接收人用户标识',
  `recipient_role` enum('student','teacher','admin') NOT NULL COMMENT '接收人角色',
  `recipient_account_no` varchar(64) NOT NULL COMMENT '接收人账号快照',
  `title` varchar(150) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `type` enum('info','success','warning','error') NOT NULL DEFAULT 'info' COMMENT '通知类型',
  `related_application_id` bigint unsigned DEFAULT NULL COMMENT '关联预约申请标识',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '通知是否已读',
  `read_at` datetime(3) DEFAULT NULL COMMENT '阅读时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_no` (`notification_no`),
  KEY `idx_recipient_read` (`recipient_user_id`,`is_read`,`created_at`),
  KEY `idx_application` (`related_application_id`),
  CONSTRAINT `fk_notifications_application` FOREIGN KEY (`related_application_id`) REFERENCES `reservation_applications` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_notifications_recipient` FOREIGN KEY (`recipient_user_id`) REFERENCES `sys_users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'N20260611001',4,'student','S20230001','教师审核通过','您的互联网+新商科实验室预约已通过教师审核，等待管理员终审。','success',2,1,'2026-06-16 13:54:23.197','2026-06-10 16:00:10.000'),(2,'N20260611002',2,'teacher','T20230001','预约审批通过','您的 402实验室 课程预约已由管理员审核通过。','success',3,1,'2026-06-09 16:00:00.000','2026-06-09 15:15:10.000'),(3,'N20260611003',5,'student','S20230002','预约申请被驳回','您的法语实验室预约因设备维护被驳回，请更换实验室后重新提交。','warning',4,0,NULL,'2026-06-08 15:30:10.000'),(4,'N20260611004',4,'student','S20230001','预约已完成','您的 IBC实验中心 预约已完成使用。','info',5,1,'2026-06-01 18:30:00.000','2026-06-01 18:00:10.000'),(5,'N20260614131515363',4,'student','S20230001','教师审核通过','您的实验室预约已通过教师审核，等待管理员终审。','success',6,1,'2026-06-16 13:54:25.297','2026-06-14 13:15:15.731'),(6,'N20260614131515829',4,'student','S20230001','预约审批通过','您的实验室预约已通过管理员终审，可按时使用。','success',6,1,'2026-06-14 13:31:42.035','2026-06-14 13:15:15.973'),(7,'N20260614131516827',2,'teacher','T20230001','预约审批通过','您的实验室预约已通过管理员终审，可按时使用。','success',7,1,'2026-06-16 13:57:33.914','2026-06-14 13:15:16.773'),(8,'N20260614134443666',4,'student','S20230001','预约审批通过','您的实验室预约已通过管理员终审，可按时使用。','success',8,1,'2026-06-16 13:54:26.246','2026-06-14 13:44:43.250'),(9,'N20260616135617522443077',4,'student','S20230001','预约申请被驳回','您的实验室预约被教师驳回：驳回示例','warning',1,1,'2026-06-16 15:16:29.619','2026-06-16 13:56:17.365'),(10,'N20260616151737950451641',4,'student','S20230001','教师审核通过','您的实验室预约已通过教师审核，等待管理员终审。','success',9,1,'2026-06-16 16:39:26.401','2026-06-16 15:17:37.466'),(11,'N20260616151828846567868',4,'student','S20230001','预约审批通过','您的实验室预约已通过管理员终审，可按时使用。','success',9,1,'2026-06-16 16:39:26.139','2026-06-16 15:18:28.367'),(12,'N20260616163843512618392',4,'student','S20230001','预约审批通过','您的实验室预约已通过管理员终审，可按时使用。','success',2,1,'2026-06-16 16:39:25.750','2026-06-16 16:38:42.702'),(13,'N20260616164118089493045',4,'student','S20230001','教师审核通过','您的实验室预约已通过教师审核，等待管理员终审。','success',10,1,'2026-06-16 17:22:40.240','2026-06-16 16:41:17.275'),(14,'N20260616164159642756028',4,'student','S20230001','预约审批未通过','您的实验室预约被管理员驳回：该时段实验室已被预约','error',10,1,'2026-06-16 17:22:40.240','2026-06-16 16:41:58.810'),(15,'N20260621194419223132560',4,'student','S20230001','预约申请已提交','您的实验室预约申请（国际课程实验室）已提交，等待教师与管理员审核，请关注审批进度。','info',11,1,'2026-06-22 22:18:44.007','2026-06-21 19:44:19.351'),(16,'N20260622222038489279619',4,'student','S20230001','预约已提交','您已成功提交「国际课程实验室」的预约申请（编号 RA20260622222037845335542），请等待审核。','info',12,1,'2026-06-22 22:20:42.954','2026-06-22 22:20:33.856'),(17,'N20260622222157650507151',4,'student','S20230001','预约申请被驳回','您的资源预约被教师驳回：1','warning',12,1,'2026-06-23 21:35:05.918','2026-06-22 22:21:53.016'),(18,'N20260622222202236197498',4,'student','S20230001','教师审核通过','您的资源预约已通过教师审核，等待管理员终审。','success',11,1,'2026-06-23 21:35:05.918','2026-06-22 22:21:57.609'),(19,'N20260622222202341239596',1,'admin','admin001','待管理员终审','张教授已初审通过王同学的预约申请，请进行管理员终审。','info',11,0,NULL,'2026-06-22 22:21:57.700'),(20,'N20260622222202371768846',6,'admin','admin002','待管理员终审','张教授已初审通过王同学的预约申请，请进行管理员终审。','info',11,0,NULL,'2026-06-22 22:21:57.730'),(21,'N20260622222237861990644',4,'student','S20230001','预约审批通过','您的资源预约已通过管理员终审，可按时使用。','success',11,1,'2026-06-23 21:35:05.918','2026-06-22 22:22:33.219');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portal_app`
--

DROP TABLE IF EXISTS `portal_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portal_app` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `app_name` varchar(80) NOT NULL COMMENT '应用名称',
  `app_code` varchar(64) NOT NULL COMMENT '应用编码，唯一',
  `category_id` bigint unsigned DEFAULT NULL COMMENT '应用分类 id',
  `icon` varchar(50) DEFAULT NULL COMMENT '应用图标标识',
  `description` varchar(255) DEFAULT NULL COMMENT '应用描述',
  `route_path` varchar(200) DEFAULT NULL COMMENT '内部路由（uni-app 页面路径或逻辑路由）',
  `external_url` varchar(500) DEFAULT NULL COMMENT '外部链接',
  `open_type` varchar(20) NOT NULL DEFAULT 'internal' COMMENT '打开方式：internal 内部路由，external 外部链接',
  `visible_roles` varchar(100) NOT NULL DEFAULT 'admin,teacher,student' COMMENT '可见角色编码，逗号分隔（兼容 sys_users.role 枚举）',
  `is_hot` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否热门',
  `is_new` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否最新',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1 启用，0 停用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号，升序',
  `visit_count` int unsigned NOT NULL DEFAULT '0' COMMENT '累计访问次数',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_code` (`app_code`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status_sort` (`status`,`sort_order`),
  KEY `idx_hot` (`is_hot`),
  KEY `idx_new` (`is_new`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-应用';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portal_app`
--

LOCK TABLES `portal_app` WRITE;
/*!40000 ALTER TABLE `portal_app` DISABLE KEYS */;
INSERT INTO `portal_app` VALUES (1,'实验室预约管理','LAB_RESERVATION',1,'calendar','在线预约实验室、查看我的预约与审批进度','/lab/reservation','','internal','admin,teacher,student',1,1,1,1,5,'2026-06-21 19:25:23.037','2026-06-26 15:46:29.980'),(2,'应用中心','APP_CENTER',1,'apps','校园服务统一入口','/portal/apps','','internal','admin,teacher,student',1,0,1,2,2,'2026-06-21 19:25:23.069','2026-06-26 15:32:29.728'),(3,'通知公告','NOTICE_CENTER',1,'notice','查看学校与平台通知公告','/notices','','internal','admin,teacher,student',0,0,1,3,0,'2026-06-21 19:25:23.100','2026-06-21 19:25:23.100'),(4,'校园资讯','NEWS_CENTER',1,'news','校园新闻与动态','/news','','internal','admin,teacher,student',0,1,1,4,1,'2026-06-21 19:25:23.131','2026-06-26 15:32:51.296'),(5,'任务中心','TASK_CENTER',1,'task','统一处理待办、已办与我发起的事项','/tasks','','internal','teacher,admin',0,0,1,5,0,'2026-06-21 19:25:23.165','2026-06-21 19:25:23.165'),(6,'消息中心','MESSAGE_CENTER',1,'message','站内消息与审批结果提醒','/messages','','internal','admin,teacher,student',0,0,1,6,0,'2026-06-21 19:25:23.197','2026-06-21 19:25:23.197'),(7,'我的日历','CALENDAR',1,'calendar','查看近期预约、审批与日程','/calendar','','internal','admin,teacher,student',0,0,1,7,3,'2026-06-21 19:25:23.228','2026-06-26 15:32:47.740'),(8,'AI 校园助手','AI_ASSISTANT',1,'ai','智能问答与服务导航','/ai/assistant','','internal','admin,teacher,student',1,1,1,8,6,'2026-06-21 19:25:23.261','2026-07-03 17:02:22.440'),(9,'管理员工作台','ADMIN_DASHBOARD',9,'dashboard','管理工作台与数据概览','/admin/dashboard','','internal','admin',0,0,1,9,0,'2026-06-21 19:25:23.292','2026-06-21 19:25:23.292'),(10,'用户管理','USER_MANAGE',9,'user','用户与账号管理','/system/users','','internal','admin',0,0,1,10,0,'2026-06-21 19:25:23.324','2026-06-21 19:25:23.324'),(11,'应用管理','APP_MANAGE',9,'apps','维护应用中心的应用与可见范围','/admin/apps','','internal','admin',0,0,1,11,0,'2026-06-21 19:25:23.355','2026-06-21 19:25:23.355'),(12,'课表查询','COURSE_QUERY',3,'book','查看我的课程表','/schedule','','internal','teacher,student',0,0,1,20,2,'2026-06-21 19:25:23.387','2026-06-26 15:31:38.690'),(13,'成绩查询','SCORE_QUERY',3,'chart','查询课程成绩（建设中）','','','internal','student',0,0,1,21,3,'2026-06-21 19:25:23.418','2026-06-26 15:32:42.868'),(14,'报修服务','REPAIR_SERVICE',6,'tools','后勤报修申请（建设中）','','','internal','admin,teacher,student',0,0,1,22,4,'2026-06-21 19:25:23.450','2026-06-26 15:33:07.573'),(15,'图书馆服务','LIBRARY_SERVICE',1,'book','图书馆门户（外部链接）','','https://www.lib.scnu.edu.cn','external','admin,teacher,student',0,0,1,23,4,'2026-06-21 19:25:23.482','2026-06-26 15:32:44.291');
/*!40000 ALTER TABLE `portal_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portal_app_category`
--

DROP TABLE IF EXISTS `portal_app_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portal_app_category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) NOT NULL COMMENT '分类编码，唯一',
  `icon` varchar(50) DEFAULT NULL COMMENT '分类图标标识',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号，升序',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1 启用，0 停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-应用分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portal_app_category`
--

LOCK TABLES `portal_app_category` WRITE;
/*!40000 ALTER TABLE `portal_app_category` DISABLE KEYS */;
INSERT INTO `portal_app_category` VALUES (1,'公共系统','public','apps',1,1,'常用公共服务入口','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(2,'学工系统','student_affairs','student',2,1,'学生事务相关服务','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(3,'教务系统','academic','book',3,1,'教学、课程、成绩相关','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(4,'宿舍系统','dormitory','home',4,1,'住宿相关服务','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(5,'教科研管理','research','flask',5,1,'科研与实验相关','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(6,'后勤管理','logistics','tools',6,1,'后勤报修等服务','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(7,'设备资产管理','asset','box',7,1,'设备与资产管理','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(8,'人事管理系统','personnel','idcard',8,1,'人事相关服务','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005'),(9,'系统管理','system','setting',99,1,'平台后台管理','2026-06-21 19:25:23.005','2026-06-21 19:25:23.005');
/*!40000 ALTER TABLE `portal_app_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portal_app_visit_log`
--

DROP TABLE IF EXISTS `portal_app_visit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portal_app_visit_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL COMMENT '访问用户 id',
  `app_id` bigint unsigned NOT NULL COMMENT '应用 id',
  `visit_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '访问时间',
  `client_type` varchar(20) DEFAULT NULL COMMENT '客户端类型：pc / miniapp / mobile',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '访问 IP',
  PRIMARY KEY (`id`),
  KEY `idx_app_time` (`app_id`,`visit_time`),
  KEY `idx_user_time` (`user_id`,`visit_time`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-访问日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portal_app_visit_log`
--

LOCK TABLES `portal_app_visit_log` WRITE;
/*!40000 ALTER TABLE `portal_app_visit_log` DISABLE KEYS */;
INSERT INTO `portal_app_visit_log` VALUES (1,4,1,'2026-06-21 19:44:17.489','pc','0:0:0:0:0:0:0:1'),(2,4,8,'2026-06-23 21:35:16.210','h5','127.0.0.1'),(3,4,14,'2026-06-23 21:35:30.507','h5','127.0.0.1'),(4,4,14,'2026-06-23 21:35:31.332','h5','127.0.0.1'),(5,4,15,'2026-06-23 21:35:32.164','h5','127.0.0.1'),(6,4,15,'2026-06-23 21:35:35.817','h5','127.0.0.1'),(7,4,13,'2026-06-23 21:35:39.377','h5','127.0.0.1'),(8,4,13,'2026-06-23 21:35:40.204','h5','127.0.0.1'),(9,4,12,'2026-06-23 21:35:40.874','h5','127.0.0.1'),(10,4,7,'2026-06-23 21:35:43.967','h5','0:0:0:0:0:0:0:1'),(11,2,8,'2026-06-23 21:36:55.903','h5','127.0.0.1'),(12,2,8,'2026-06-23 21:36:57.518','h5','127.0.0.1'),(13,4,8,'2026-06-26 15:19:54.458','h5','0:0:0:0:0:0:0:1'),(14,4,12,'2026-06-26 15:31:38.658','h5','0:0:0:0:0:0:0:1'),(15,4,7,'2026-06-26 15:31:49.292','h5','0:0:0:0:0:0:0:1'),(16,4,2,'2026-06-26 15:32:28.949','h5','0:0:0:0:0:0:0:1'),(17,4,2,'2026-06-26 15:32:29.694','h5','0:0:0:0:0:0:0:1'),(18,4,15,'2026-06-26 15:32:37.752','h5','0:0:0:0:0:0:0:1'),(19,4,14,'2026-06-26 15:32:41.161','h5','0:0:0:0:0:0:0:1'),(20,4,13,'2026-06-26 15:32:42.834','h5','0:0:0:0:0:0:0:1'),(21,4,15,'2026-06-26 15:32:44.261','h5','0:0:0:0:0:0:0:1'),(22,4,7,'2026-06-26 15:32:47.706','h5','0:0:0:0:0:0:0:1'),(23,4,4,'2026-06-26 15:32:51.265','h5','0:0:0:0:0:0:0:1'),(24,4,8,'2026-06-26 15:32:53.987','h5','0:0:0:0:0:0:0:1'),(25,4,14,'2026-06-26 15:33:07.541','h5','0:0:0:0:0:0:0:1'),(26,4,1,'2026-06-26 15:34:09.457','h5','0:0:0:0:0:0:0:1'),(27,4,1,'2026-06-26 15:34:11.416','h5','0:0:0:0:0:0:0:1'),(28,2,1,'2026-06-26 15:38:36.579','h5','0:0:0:0:0:0:0:1'),(29,2,1,'2026-06-26 15:46:29.950','h5','0:0:0:0:0:0:0:1'),(30,4,8,'2026-07-03 17:02:22.400','h5','0:0:0:0:0:0:0:1');
/*!40000 ALTER TABLE `portal_app_visit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portal_user_app_favorite`
--

DROP TABLE IF EXISTS `portal_user_app_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portal_user_app_favorite` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '用户 id（sys_users.id）',
  `app_id` bigint unsigned NOT NULL COMMENT '应用 id（portal_app.id）',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_app` (`user_id`,`app_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-用户收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portal_user_app_favorite`
--

LOCK TABLES `portal_user_app_favorite` WRITE;
/*!40000 ALTER TABLE `portal_user_app_favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `portal_user_app_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation_applications`
--

DROP TABLE IF EXISTS `reservation_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_applications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '预约申请主键',
  `application_no` varchar(64) NOT NULL COMMENT '预约申请业务编号',
  `applicant_user_id` bigint unsigned NOT NULL COMMENT '申请人用户标识',
  `applicant_role` enum('student','teacher') NOT NULL COMMENT '申请人角色',
  `applicant_no` varchar(64) NOT NULL COMMENT '申请人账号快照',
  `applicant_name` varchar(50) NOT NULL COMMENT '申请人姓名快照',
  `applicant_phone` varchar(20) DEFAULT NULL COMMENT '申请人手机号快照',
  `lab_id` bigint unsigned DEFAULT NULL COMMENT '????????????????????',
  `lab_name_snapshot` varchar(120) NOT NULL COMMENT '??????????',
  `resource_id` bigint unsigned DEFAULT NULL COMMENT '统一资源主键',
  `resource_name_snapshot` varchar(120) DEFAULT NULL COMMENT '资源名称快照',
  `schedule_id` bigint unsigned DEFAULT NULL COMMENT '基于课表预约时关联课程课表主键',
  `reserve_date` date NOT NULL COMMENT '预约日期',
  `start_time` time NOT NULL COMMENT '预约开始时间',
  `end_time` time NOT NULL COMMENT '预约结束时间',
  `time_slot_label` varchar(50) DEFAULT NULL COMMENT '显示用时间段标签',
  `participant_count` int unsigned NOT NULL DEFAULT '1' COMMENT '参与人数',
  `application_type` varchar(50) DEFAULT NULL COMMENT '学生预约类型编码',
  `application_type_name` varchar(50) DEFAULT NULL COMMENT '学生预约类型名称',
  `title` varchar(150) DEFAULT NULL COMMENT '学生预约主题',
  `purpose` text COMMENT '使用目的',
  `instructor_name` varchar(50) DEFAULT NULL COMMENT '指导教师或负责教师姓名',
  `requirements` text COMMENT '特殊需求',
  `emergency_contact_name` varchar(50) DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_contact_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系人电话',
  `course_name` varchar(150) DEFAULT NULL COMMENT '教师预约课程名称',
  `course_type` varchar(50) DEFAULT NULL COMMENT '教师预约课程类型',
  `remark` text COMMENT '备注',
  `status` enum('draft','pending','teacher_approved','approved','rejected','cancelled','completed') NOT NULL DEFAULT 'draft' COMMENT '当前流程状态',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已使用完成',
  `submitted_at` datetime(3) DEFAULT NULL COMMENT '申请提交时间',
  `teacher_review_user_id` bigint unsigned DEFAULT NULL COMMENT '教师审核人用户标识',
  `teacher_review_name` varchar(50) DEFAULT NULL COMMENT '教师审核人姓名快照',
  `teacher_review_at` datetime(3) DEFAULT NULL COMMENT '教师审核时间',
  `teacher_review_comment` varchar(500) DEFAULT NULL COMMENT '教师审核意见',
  `admin_review_user_id` bigint unsigned DEFAULT NULL COMMENT '管理员审核人用户标识',
  `admin_review_name` varchar(50) DEFAULT NULL COMMENT '管理员审核人姓名快照',
  `admin_review_at` datetime(3) DEFAULT NULL COMMENT '管理员审核时间',
  `admin_review_comment` varchar(500) DEFAULT NULL COMMENT '管理员审核意见',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '最近一次驳回原因',
  `cancelled_at` datetime(3) DEFAULT NULL COMMENT '取消时间',
  `completed_at` datetime(3) DEFAULT NULL COMMENT '完成时间',
  `source_application_id` bigint unsigned DEFAULT NULL COMMENT '重新申请或修改来源申请标识',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_applicant_status` (`applicant_user_id`,`status`),
  KEY `idx_lab_date_time` (`lab_id`,`reserve_date`,`start_time`,`end_time`),
  KEY `idx_status_submitted` (`status`,`submitted_at`),
  KEY `idx_teacher_review` (`teacher_review_user_id`,`teacher_review_at`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_admin_review_user_id` (`admin_review_user_id`),
  KEY `idx_source_application_id` (`source_application_id`),
  KEY `idx_resource_date_time` (`resource_id`,`reserve_date`,`start_time`,`end_time`),
  CONSTRAINT `fk_reservation_admin_reviewer` FOREIGN KEY (`admin_review_user_id`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_applicant` FOREIGN KEY (`applicant_user_id`) REFERENCES `sys_users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_lab` FOREIGN KEY (`lab_id`) REFERENCES `laboratories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_resource` FOREIGN KEY (`resource_id`) REFERENCES `campus_resources` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `course_schedules` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_source_application` FOREIGN KEY (`source_application_id`) REFERENCES `reservation_applications` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_teacher_reviewer` FOREIGN KEY (`teacher_review_user_id`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_reservation_participant_count` CHECK ((`participant_count` > 0)),
  CONSTRAINT `chk_reservation_time` CHECK ((`end_time` > `start_time`))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生与教师统一预约申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_applications`
--

LOCK TABLES `reservation_applications` WRITE;
/*!40000 ALTER TABLE `reservation_applications` DISABLE KEYS */;
INSERT INTO `reservation_applications` VALUES (1,'RA20260611001',4,'student','S20230001','王同学','13900139001',1,'国际课程实验室',1,'国际课程实验室',NULL,'2026-06-15','08:00:00','09:50:00','08:00-09:50',6,'course','课程实验','课程设计小组讨论','需要使用实验室进行课程设计讨论和原型验证。','张教授','投影仪、白板','王同学','13900139001',NULL,NULL,NULL,'rejected',0,'2026-06-11 09:00:00.000',2,'张教授','2026-06-16 13:56:17.264','驳回示例',NULL,NULL,NULL,NULL,'驳回示例',NULL,NULL,NULL,'2026-06-11 09:00:00.000','2026-06-20 19:26:21.590',NULL),(2,'RA20260611002',4,'student','S20230001','王同学','13900139001',3,'互联网+新商科实验室',3,'互联网+新商科实验室',1,'2026-06-16','14:00:00','15:50:00','14:00-15:50',10,'research','科研项目','机器学习算法实验','需要 GPU 工作站完成模型训练。','张教授','GPU 工作站、投影仪','王同学','13900139001',NULL,NULL,NULL,'approved',0,'2026-06-10 14:20:00.000',2,'张教授','2026-06-10 16:00:00.000','研究内容合理，同意提交管理员终审。',1,'系统管理员','2026-06-16 16:38:42.603',NULL,NULL,NULL,NULL,NULL,'2026-06-10 14:20:00.000','2026-06-20 19:26:21.590',NULL),(3,'RA20260611003',2,'teacher','T20230001','张教授','13800138001',5,'402实验室',5,'402实验室',4,'2026-06-17','10:00:00','11:50:00','10:00-11:50',32,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'数据库系统实验课','实验课','需要提前安装 MySQL 和建模工具。','approved',0,'2026-06-09 10:30:00.000',NULL,NULL,NULL,NULL,6,'审核管理员','2026-06-09 15:15:00.000','课程安排明确，审批通过。',NULL,NULL,NULL,NULL,'2026-06-09 10:30:00.000','2026-06-20 19:26:21.590',NULL),(4,'RA20260611004',5,'student','S20230002','赵同学','13900139002',4,'法语实验室',4,'法语实验室',NULL,'2026-06-18','19:00:00','20:50:00','19:00-20:50',8,'activity','学术活动','社团分享会','计划举办小型语言文化分享活动。','李老师','录播设备','赵同学','13900139002',NULL,NULL,NULL,'rejected',0,'2026-06-08 11:20:00.000',3,'李老师','2026-06-08 15:30:00.000','该实验室设备维护中，请更换实验室。',NULL,NULL,NULL,NULL,'该实验室设备维护中，请更换实验室。',NULL,NULL,NULL,'2026-06-08 11:20:00.000','2026-06-20 19:26:21.590',NULL),(5,'RA20260611005',4,'student','S20230001','王同学','13900139001',2,'IBC实验中心',2,'IBC实验中心',NULL,'2026-06-01','16:00:00','17:50:00','16:00-17:50',12,'competition','竞赛培训','算法竞赛训练','团队进行赛前模拟训练。','张教授','公共自习区、投影仪','王同学','13900139001',NULL,NULL,NULL,'completed',1,'2026-05-28 10:10:00.000',2,'张教授','2026-05-28 11:00:00.000','同意使用。',6,'审核管理员','2026-05-28 14:00:00.000','审批通过。',NULL,NULL,'2026-06-01 18:00:00.000',NULL,'2026-05-28 10:10:00.000','2026-06-20 19:26:21.590',NULL),(6,'RA20260614131515657',4,'student','S20230001','王同学','13900139001',1,'国际课程实验室',1,'国际课程实验室',NULL,'2026-06-25','08:00:00','09:50:00','08:00-09:50',5,'course','课程实验','自动化测试预约','端到端测试','张教授',NULL,NULL,NULL,NULL,NULL,NULL,'completed',1,'2026-06-14 13:15:15.340',2,'张教授','2026-06-14 13:15:15.693','测试通过',1,'系统管理员','2026-06-14 13:15:15.926','终审通过',NULL,NULL,'2026-06-14 13:15:16.238',NULL,'2026-06-14 13:15:15.340','2026-06-20 19:26:21.590',NULL),(7,'RA20260614131516665',2,'teacher','T20230001','张教授','13800138001',2,'IBC实验中心',2,'IBC实验中心',NULL,'2026-06-26','14:00:00','15:50:00','14:00-15:50',20,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'测试课程','实验课','教师自助预约测试','approved',0,'2026-06-14 13:15:16.503',NULL,NULL,NULL,NULL,1,'系统管理员','2026-06-14 13:15:16.733','ok',NULL,NULL,NULL,NULL,'2026-06-14 13:15:16.503','2026-06-20 19:26:21.590',NULL),(8,'RA20260614134442106',4,'student','S20230001','王同学','13900139001',1,'国际课程实验室',1,'国际课程实验室',NULL,'2026-09-30','08:00:00','09:00:00','08:00-09:00',5,'course','课程实验','测试审批绕过','audit test',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'approved',0,'2026-06-14 13:44:43.003',NULL,NULL,NULL,NULL,1,'系统管理员','2026-06-14 13:44:43.216','admin bypass teacher',NULL,NULL,NULL,NULL,'2026-06-14 13:44:43.003','2026-06-20 19:26:21.590',NULL),(9,'RA20260616151658804953912',4,'student','S20230001','王同学','13900139001',1,'国际课程实验室',1,'国际课程实验室',NULL,'2026-06-16','08:00:00','09:50:00','08:00-09:50',4,'other','其他','1','1','','1','1','1',NULL,NULL,NULL,'approved',0,'2026-06-16 15:16:58.338',2,'张教授','2026-06-16 15:17:37.356','1',1,'系统管理员','2026-06-16 15:18:28.255',NULL,NULL,NULL,NULL,NULL,'2026-06-16 15:16:58.338','2026-06-20 19:26:21.590',NULL),(10,'RA20260616164028580250082',4,'student','S20230001','王同学','13900139001',1,'国际课程实验室',1,'国际课程实验室',NULL,'2026-06-16','14:00:00','22:13:00','14:00-22:13',13,'course','课程实验','111','111','老师','1','。','2',NULL,NULL,NULL,'rejected',0,'2026-06-16 16:40:27.768',2,'张教授','2026-06-16 16:41:17.178','1',1,'系统管理员','2026-06-16 16:41:58.710','该时段实验室已被预约','该时段实验室已被预约',NULL,NULL,NULL,'2026-06-16 16:40:27.768','2026-06-20 19:26:21.590',NULL),(11,'RA20260621194419186776141',4,'student','S20230001','王同学','13900139000',1,'国际课程实验室',1,'国际课程实验室',NULL,'2026-06-29','19:00:00','21:00:00','19:00-21:00',12,'course','课程实验','集成测试-课程实验','课程实验测试','张教授','投影','李同学','13900139001',NULL,NULL,NULL,'approved',0,'2026-06-21 19:44:19.317',2,'张教授','2026-06-22 22:21:57.517',NULL,1,'系统管理员','2026-06-22 22:22:33.129',NULL,NULL,NULL,NULL,NULL,'2026-06-21 19:44:19.317','2026-06-22 22:22:33.129',NULL),(12,'RA20260622222037845335542',4,'student','S20230001','王同学','13900139002',1,'国际课程实验室',1,'国际课程实验室',NULL,'2026-06-22','09:00:00','09:50:00','09:00-09:50',1,'course','课程实验','1','1','1','','','',NULL,NULL,NULL,'rejected',0,'2026-06-22 22:20:33.245',2,'张教授','2026-06-22 22:21:52.925','1',NULL,NULL,NULL,NULL,'1',NULL,NULL,NULL,'2026-06-22 22:20:33.245','2026-06-22 22:21:52.925',NULL);
/*!40000 ALTER TABLE `reservation_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation_approval_logs`
--

DROP TABLE IF EXISTS `reservation_approval_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_approval_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '预约审批日志主键',
  `application_id` bigint unsigned NOT NULL COMMENT '关联预约申请表主键',
  `stage` enum('teacher','admin','system') NOT NULL COMMENT '流程阶段',
  `action` enum('submit','approve','reject','cancel','complete','reprocess') NOT NULL COMMENT '流程动作',
  `from_status` varchar(32) DEFAULT NULL COMMENT '原状态',
  `to_status` varchar(32) NOT NULL COMMENT '新状态',
  `reviewer_user_id` bigint unsigned DEFAULT NULL COMMENT '操作人用户标识；系统动作时为空',
  `reviewer_name_snapshot` varchar(50) DEFAULT NULL COMMENT '操作人姓名快照',
  `comment` varchar(500) DEFAULT NULL COMMENT '审核意见或操作原因',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_created` (`application_id`,`created_at`),
  KEY `idx_reviewer_created` (`reviewer_user_id`,`created_at`),
  CONSTRAINT `fk_reservation_approval_logs_application` FOREIGN KEY (`application_id`) REFERENCES `reservation_applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_approval_logs_reviewer` FOREIGN KEY (`reviewer_user_id`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约流程审批日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_approval_logs`
--

LOCK TABLES `reservation_approval_logs` WRITE;
/*!40000 ALTER TABLE `reservation_approval_logs` DISABLE KEYS */;
INSERT INTO `reservation_approval_logs` VALUES (1,1,'system','submit',NULL,'pending',4,'王同学','学生提交预约申请。','2026-06-11 09:00:00.000'),(2,2,'system','submit',NULL,'pending',4,'王同学','学生提交科研项目预约。','2026-06-10 14:20:00.000'),(3,2,'teacher','approve','pending','teacher_approved',2,'张教授','研究内容合理，同意提交管理员终审。','2026-06-10 16:00:00.000'),(4,3,'system','submit',NULL,'pending',2,'张教授','教师提交课程预约。','2026-06-09 10:30:00.000'),(5,3,'admin','approve','pending','approved',6,'审核管理员','课程安排明确，审批通过。','2026-06-09 15:15:00.000'),(6,4,'system','submit',NULL,'pending',5,'赵同学','学生提交学术活动预约。','2026-06-08 11:20:00.000'),(7,4,'teacher','reject','pending','rejected',3,'李老师','该实验室设备维护中，请更换实验室。','2026-06-08 15:30:00.000'),(8,5,'system','submit',NULL,'pending',4,'王同学','学生提交竞赛培训预约。','2026-05-28 10:10:00.000'),(9,5,'teacher','approve','pending','teacher_approved',2,'张教授','同意使用。','2026-05-28 11:00:00.000'),(10,5,'admin','approve','teacher_approved','approved',6,'审核管理员','审批通过。','2026-05-28 14:00:00.000'),(11,5,'system','complete','approved','completed',NULL,'系统','预约日期已结束，自动标记完成。','2026-06-01 18:00:00.000'),(12,6,'system','submit',NULL,'pending',4,'王同学','学生提交预约申请','2026-06-14 13:15:15.366'),(13,6,'teacher','approve','pending','teacher_approved',2,'张教授','测试通过','2026-06-14 13:15:15.713'),(14,6,'admin','approve','teacher_approved','approved',1,'系统管理员','终审通过','2026-06-14 13:15:15.946'),(15,6,'system','complete','approved','completed',4,'王同学','申请人标记使用完成','2026-06-14 13:15:16.263'),(16,7,'system','submit',NULL,'pending',2,'张教授','教师提交预约申请','2026-06-14 13:15:16.518'),(17,7,'admin','approve','pending','approved',1,'系统管理员','ok','2026-06-14 13:15:16.753'),(18,8,'system','submit',NULL,'pending',4,'王同学','学生提交预约申请','2026-06-14 13:44:43.021'),(19,8,'admin','approve','pending','approved',1,'系统管理员','admin bypass teacher','2026-06-14 13:44:43.233'),(20,1,'teacher','reject','pending','rejected',2,'张教授','驳回示例','2026-06-16 13:56:17.312'),(21,9,'system','submit',NULL,'pending',4,'王同学','学生提交预约申请','2026-06-16 15:16:58.399'),(22,9,'teacher','approve','pending','teacher_approved',2,'张教授','1','2026-06-16 15:17:37.413'),(23,9,'admin','approve','teacher_approved','approved',1,'系统管理员',NULL,'2026-06-16 15:18:28.305'),(24,2,'admin','approve','teacher_approved','approved',1,'系统管理员',NULL,'2026-06-16 16:38:42.657'),(25,10,'system','submit',NULL,'pending',4,'王同学','学生提交预约申请','2026-06-16 16:40:27.811'),(26,10,'teacher','approve','pending','teacher_approved',2,'张教授','1','2026-06-16 16:41:17.220'),(27,10,'admin','reject','teacher_approved','rejected',1,'系统管理员','该时段实验室已被预约','2026-06-16 16:41:58.763'),(28,11,'system','submit',NULL,'pending',4,'王同学','学生提交预约申请','2026-06-21 19:44:19.334'),(29,12,'system','submit',NULL,'pending',4,'王同学','学生提交预约申请','2026-06-22 22:20:33.282'),(30,12,'teacher','reject','pending','rejected',2,'张教授','1','2026-06-22 22:21:52.956'),(31,11,'teacher','approve','pending','teacher_approved',2,'张教授',NULL,'2026-06-22 22:21:57.548'),(32,11,'admin','approve','teacher_approved','approved',1,'系统管理员',NULL,'2026-06-22 22:22:33.159');
/*!40000 ALTER TABLE `reservation_approval_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation_attachments`
--

DROP TABLE IF EXISTS `reservation_attachments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_attachments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '预约附件主键',
  `application_id` bigint unsigned NOT NULL COMMENT '关联预约申请表主键',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) NOT NULL COMMENT '文件地址或对象存储键',
  `file_type` varchar(80) DEFAULT NULL COMMENT '媒体类型或扩展名',
  `file_size_bytes` bigint unsigned DEFAULT NULL COMMENT '文件大小，单位字节',
  `uploaded_by` bigint unsigned DEFAULT NULL COMMENT '上传人用户标识',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_application` (`application_id`),
  KEY `idx_uploaded_by` (`uploaded_by`),
  CONSTRAINT `fk_reservation_attachments_application` FOREIGN KEY (`application_id`) REFERENCES `reservation_applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reservation_attachments_uploaded_by` FOREIGN KEY (`uploaded_by`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约附件文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_attachments`
--

LOCK TABLES `reservation_attachments` WRITE;
/*!40000 ALTER TABLE `reservation_attachments` DISABLE KEYS */;
INSERT INTO `reservation_attachments` VALUES (1,1,'课程设计计划.docx','/uploads/reservations/RA20260611001-plan.docx','application/vnd.openxmlformats-officedocument.wordprocessingml.document',524288,4,'2026-06-11 09:00:10.000'),(2,3,'数据库实验安排.pdf','/uploads/reservations/RA20260611003-schedule.pdf','application/pdf',786432,2,'2026-06-09 10:30:15.000');
/*!40000 ALTER TABLE `reservation_attachments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule_export_tasks`
--

DROP TABLE IF EXISTS `schedule_export_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_export_tasks` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '课表导出任务主键',
  `task_no` varchar(64) NOT NULL COMMENT '导出任务编号',
  `semester_id` bigint unsigned NOT NULL COMMENT '关联学期表主键',
  `export_format` enum('excel','pdf') NOT NULL COMMENT '导出文件格式',
  `include_rooms` tinyint(1) NOT NULL DEFAULT '1' COMMENT '导出数据是否包含教室',
  `include_teachers` tinyint(1) NOT NULL DEFAULT '1' COMMENT '导出数据是否包含教师',
  `include_students` tinyint(1) NOT NULL DEFAULT '0' COMMENT '导出数据是否包含学生',
  `file_url` varchar(500) DEFAULT NULL COMMENT '生成文件地址',
  `status` enum('pending','processing','success','failed') NOT NULL DEFAULT 'pending' COMMENT '导出任务状态',
  `error_message` text COMMENT '导出失败信息',
  `created_by` bigint unsigned NOT NULL COMMENT '创建导出任务的管理员用户标识',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `finished_at` datetime(3) DEFAULT NULL COMMENT '任务完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_semester_created` (`semester_id`,`created_at`),
  KEY `idx_created_by` (`created_by`),
  CONSTRAINT `fk_schedule_export_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_schedule_export_semester` FOREIGN KEY (`semester_id`) REFERENCES `academic_semesters` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课表导出任务记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule_export_tasks`
--

LOCK TABLES `schedule_export_tasks` WRITE;
/*!40000 ALTER TABLE `schedule_export_tasks` DISABLE KEYS */;
INSERT INTO `schedule_export_tasks` VALUES (1,'SET20260611001',1,'excel',1,1,0,'/exports/schedules/2025-2026-2.xlsx','success',NULL,1,'2026-06-09 15:00:00.000','2026-06-09 15:00:25.000'),(2,'SET20260614133141693',1,'excel',1,1,0,'/exports/schedules/SET20260614133141693.xlsx','success',NULL,1,'2026-06-14 13:31:41.499','2026-06-14 13:31:41.499'),(3,'SET20260616134325732532679',2,'excel',1,1,0,'/exports/schedules/SET20260616134325732532679.xlsx','success',NULL,1,'2026-06-16 13:43:25.651','2026-06-16 13:43:25.651'),(4,'SET20260621180703695363426',2,'excel',1,1,0,'/exports/schedules/SET20260621180703695363426.xlsx','success',NULL,1,'2026-06-21 18:06:59.810','2026-06-21 18:06:59.810');
/*!40000 ALTER TABLE `schedule_export_tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule_import_batches`
--

DROP TABLE IF EXISTS `schedule_import_batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_import_batches` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '课表导入批次主键',
  `batch_no` varchar(64) NOT NULL COMMENT '导入批次编号',
  `semester_id` bigint unsigned NOT NULL COMMENT '关联学期表主键',
  `file_name` varchar(255) NOT NULL COMMENT '上传文件名',
  `file_size_bytes` bigint unsigned DEFAULT NULL COMMENT '上传文件大小，单位字节',
  `file_path` varchar(500) DEFAULT NULL COMMENT '存储文件路径',
  `status` enum('pending','parsing','validating','saving','success','failed') NOT NULL DEFAULT 'pending' COMMENT '导入状态',
  `progress` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '导入进度百分比',
  `success_count` int unsigned NOT NULL DEFAULT '0' COMMENT '成功行数',
  `failure_count` int unsigned NOT NULL DEFAULT '0' COMMENT '失败行数',
  `error_details` text COMMENT '导入错误详情',
  `imported_by` bigint unsigned NOT NULL COMMENT '执行课表导入的管理员用户标识',
  `started_at` datetime(3) DEFAULT NULL COMMENT '导入开始时间',
  `finished_at` datetime(3) DEFAULT NULL COMMENT '导入结束时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_semester_status` (`semester_id`,`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_imported_by` (`imported_by`),
  CONSTRAINT `fk_schedule_import_imported_by` FOREIGN KEY (`imported_by`) REFERENCES `sys_users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_schedule_import_semester` FOREIGN KEY (`semester_id`) REFERENCES `academic_semesters` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_schedule_import_progress` CHECK ((`progress` <= 100))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课表导入批次记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule_import_batches`
--

LOCK TABLES `schedule_import_batches` WRITE;
/*!40000 ALTER TABLE `schedule_import_batches` DISABLE KEYS */;
INSERT INTO `schedule_import_batches` VALUES (1,'SIB20260611001',1,'2025-2026-2学期课表.xlsx',262144,'/uploads/schedules/2025-2026-2.xlsx','success',100,128,0,NULL,1,'2026-06-01 09:00:00.000','2026-06-01 09:02:35.000','2026-06-01 09:00:00.000','2026-06-01 09:02:35.000'),(2,'SIB20260611002',1,'错误示例课表.xlsx',98304,'/uploads/schedules/error-sample.xlsx','failed',100,0,3,'第5行教室信息不完整；第8行时间格式错误。',1,'2026-06-08 14:00:00.000','2026-06-08 14:01:20.000','2026-06-08 14:00:00.000','2026-06-08 14:01:20.000'),(3,'SIB20260614133141920',1,'test.xlsx',NULL,NULL,'success',100,4,0,NULL,1,'2026-06-14 13:31:41.425','2026-06-14 13:31:41.425','2026-06-14 13:31:41.425','2026-06-14 13:31:41.425'),(4,'SIB20260616134317690794877',2,'2023-2024-1学期课表.xlsx',NULL,NULL,'success',100,0,0,NULL,1,'2026-06-16 13:43:17.602','2026-06-16 13:43:17.602','2026-06-16 13:43:17.602','2026-06-16 13:43:17.602'),(5,'SIB20260621180653790550582',2,'2023-2024-1学期课表.xlsx',NULL,NULL,'success',100,0,0,NULL,1,'2026-06-21 18:06:49.911','2026-06-21 18:06:49.911','2026-06-21 18:06:49.911','2026-06-21 18:06:49.911');
/*!40000 ALTER TABLE `schedule_import_batches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_profiles`
--

DROP TABLE IF EXISTS `student_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_profiles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '学生资料主键',
  `user_id` bigint unsigned NOT NULL COMMENT '关联用户表主键',
  `student_no` varchar(64) NOT NULL COMMENT '学号',
  `college` varchar(100) NOT NULL COMMENT '学院名称',
  `major` varchar(100) NOT NULL COMMENT '年级与专业文本',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  CONSTRAINT `fk_student_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `sys_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生扩展资料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_profiles`
--

LOCK TABLES `student_profiles` WRITE;
/*!40000 ALTER TABLE `student_profiles` DISABLE KEYS */;
INSERT INTO `student_profiles` VALUES (1,4,'S20230001','计算机学院','2023级软件工程','2026-03-10 09:00:00.000','2026-06-11 08:45:00.000'),(2,5,'S20230002','经济与管理学院','2023级信息管理与信息系统','2026-03-11 09:00:00.000','2026-06-08 13:20:00.000'),(3,8,'S99test01','计算机学院','2024级测试','2026-06-14 13:32:38.361','2026-06-14 13:32:38.361');
/*!40000 ALTER TABLE `student_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menus`
--

DROP TABLE IF EXISTS `sys_menus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menus` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '菜单主键',
  `parent_id` bigint unsigned DEFAULT NULL COMMENT '父级菜单',
  `menu_code` varchar(120) NOT NULL COMMENT '菜单编码',
  `menu_name` varchar(120) NOT NULL COMMENT '菜单名称',
  `path` varchar(255) DEFAULT NULL COMMENT '前端路由',
  `icon` varchar(120) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `permission_code` varchar(120) DEFAULT NULL COMMENT '进入菜单需要的权限编码',
  `visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示',
  `status` enum('active','disabled','deleted') NOT NULL DEFAULT 'active' COMMENT '菜单状态',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`),
  KEY `idx_parent_sort` (`parent_id`,`sort_order`),
  CONSTRAINT `fk_sys_menus_parent` FOREIGN KEY (`parent_id`) REFERENCES `sys_menus` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menus`
--

LOCK TABLES `sys_menus` WRITE;
/*!40000 ALTER TABLE `sys_menus` DISABLE KEYS */;
INSERT INTO `sys_menus` VALUES (1,NULL,'admin-work','?????','/pages/admin-work/admin-work','dashboard',10,'dashboard:view',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(2,NULL,'admin-users','?????','/pages/admin-system-management/admin-system-management','users',20,'user:manage',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(3,NULL,'admin-labs','?????','/pages/admin-lab-management/admin-lab-management','building',30,'lab:manage',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(4,NULL,'admin-schedules','????','/pages/admin-schedule-management/admin-schedule-management','calendar',40,'schedule:manage',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(5,NULL,'admin-reservations','????','/pages/admin-reservation-approval/admin-reservation-approval','check',50,'reservation:admin_review',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(6,NULL,'student-work','?????','/pages/student-work/student-work','home',10,'dashboard:view',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(7,NULL,'student-reservation','????','/pages/student-reservation-apply/student-reservation-apply','calendar-plus',20,'reservation:create',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(8,NULL,'student-notifications','????','/pages/student-notifications/student-notifications','bell',30,'notification:view',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(9,NULL,'teacher-work','?????','/pages/teacher-work/teacher-work','home',10,'dashboard:view',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(10,NULL,'teacher-review','????','/pages/teacher-pending-process/teacher-pending-process','check',20,'reservation:teacher_review',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(11,NULL,'teacher-notifications','????','/pages/teacher-notifications/teacher-notifications','bell',30,'notification:view',1,'active','2026-06-20 15:23:36.117','2026-06-22 22:07:03.136'),(12,NULL,'admin-rbac','????','/pages/admin-rbac-management/admin-rbac-management','shield',25,'rbac:manage',1,'active','2026-06-22 22:07:02.801','2026-06-22 22:07:02.801'),(13,NULL,'admin-notifications','????','/pages/admin-notifications/admin-notifications','bell',60,'notification:view',1,'active','2026-06-22 22:07:02.801','2026-06-22 22:07:02.801');
/*!40000 ALTER TABLE `sys_menus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_permissions`
--

DROP TABLE IF EXISTS `sys_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permissions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '权限主键',
  `permission_code` varchar(120) NOT NULL COMMENT '权限编码',
  `permission_name` varchar(120) NOT NULL COMMENT '权限名称',
  `module` varchar(64) NOT NULL COMMENT '所属模块',
  `action` varchar(64) NOT NULL COMMENT '操作类型',
  `description` varchar(255) DEFAULT NULL COMMENT '权限说明',
  `status` enum('active','disabled','deleted') NOT NULL DEFAULT 'active' COMMENT '权限状态',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_module_status` (`module`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统权限点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permissions`
--

LOCK TABLES `sys_permissions` WRITE;
/*!40000 ALTER TABLE `sys_permissions` DISABLE KEYS */;
INSERT INTO `sys_permissions` VALUES (1,'dashboard:view','?????','dashboard','view','???????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(2,'user:manage','????','user','manage','?????????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(3,'rbac:manage','????','rbac','manage','??????????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(4,'lab:manage','?????','lab','manage','???????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(5,'schedule:manage','????','schedule','manage','????????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(6,'reservation:create','????','reservation','create','??????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(7,'reservation:view_own','??????','reservation','view_own','????????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(8,'reservation:teacher_review','??????','reservation','teacher_review','????????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(9,'reservation:admin_review','???????','reservation','admin_review','???????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(10,'notification:view','????','notification','view','??????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102'),(11,'system:manage','????','system','manage','??????????','active','2026-06-20 15:23:36.083','2026-06-22 22:07:03.102');
/*!40000 ALTER TABLE `sys_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menus`
--

DROP TABLE IF EXISTS `sys_role_menus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menus` (
  `role_id` bigint unsigned NOT NULL COMMENT '角色主键',
  `menu_id` bigint unsigned NOT NULL COMMENT '菜单主键',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`role_id`,`menu_id`),
  KEY `idx_menu_id` (`menu_id`),
  CONSTRAINT `fk_sys_role_menus_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menus` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sys_role_menus_role` FOREIGN KEY (`role_id`) REFERENCES `sys_roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menus`
--

LOCK TABLES `sys_role_menus` WRITE;
/*!40000 ALTER TABLE `sys_role_menus` DISABLE KEYS */;
INSERT INTO `sys_role_menus` VALUES (1,1,'2026-06-20 15:23:36.254'),(1,2,'2026-06-20 15:23:36.254'),(1,3,'2026-06-20 15:23:36.254'),(1,4,'2026-06-20 15:23:36.254'),(1,5,'2026-06-20 15:23:36.254'),(1,12,'2026-06-22 22:07:02.930'),(1,13,'2026-06-22 22:07:02.930'),(2,9,'2026-06-20 15:23:36.287'),(2,10,'2026-06-20 15:23:36.287'),(2,11,'2026-06-20 15:23:36.287'),(3,6,'2026-06-20 15:23:36.320'),(3,7,'2026-06-20 15:23:36.320'),(3,8,'2026-06-20 15:23:36.320');
/*!40000 ALTER TABLE `sys_role_menus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_permissions`
--

DROP TABLE IF EXISTS `sys_role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permissions` (
  `role_id` bigint unsigned NOT NULL COMMENT '角色主键',
  `permission_id` bigint unsigned NOT NULL COMMENT '权限主键',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_sys_role_permissions_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sys_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `sys_roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_permissions`
--

LOCK TABLES `sys_role_permissions` WRITE;
/*!40000 ALTER TABLE `sys_role_permissions` DISABLE KEYS */;
INSERT INTO `sys_role_permissions` VALUES (1,1,'2026-06-20 15:23:36.152'),(1,2,'2026-06-20 15:23:36.152'),(1,3,'2026-06-20 15:23:36.152'),(1,4,'2026-06-20 15:23:36.152'),(1,5,'2026-06-20 15:23:36.152'),(1,6,'2026-06-20 15:23:36.152'),(1,7,'2026-06-20 15:23:36.152'),(1,8,'2026-06-20 15:23:36.152'),(1,9,'2026-06-20 15:23:36.152'),(1,10,'2026-06-20 15:23:36.152'),(1,11,'2026-06-20 15:23:36.152'),(2,1,'2026-06-20 15:23:36.187'),(2,6,'2026-06-20 15:23:36.187'),(2,7,'2026-06-20 15:23:36.187'),(2,8,'2026-06-20 15:23:36.187'),(2,10,'2026-06-20 15:23:36.187'),(3,1,'2026-06-20 15:23:36.220'),(3,6,'2026-06-20 15:23:36.220'),(3,7,'2026-06-20 15:23:36.220'),(3,10,'2026-06-20 15:23:36.220');
/*!40000 ALTER TABLE `sys_role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_roles`
--

DROP TABLE IF EXISTS `sys_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_roles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '角色主键',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` varchar(255) DEFAULT NULL COMMENT '角色说明',
  `status` enum('active','disabled','deleted') NOT NULL DEFAULT 'active' COMMENT '角色状态',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_roles`
--

LOCK TABLES `sys_roles` WRITE;
/*!40000 ALTER TABLE `sys_roles` DISABLE KEYS */;
INSERT INTO `sys_roles` VALUES (1,'admin','???','????????????','active',10,'2026-06-20 15:23:36.049','2026-06-22 22:07:03.068'),(2,'teacher','??','????????????','active',20,'2026-06-20 15:23:36.049','2026-06-22 22:07:03.068'),(3,'student','??','???????????????','active',30,'2026-06-20 15:23:36.049','2026-06-22 22:07:03.068');
/*!40000 ALTER TABLE `sys_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_roles`
--

DROP TABLE IF EXISTS `sys_user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_roles` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户主键',
  `role_id` bigint unsigned NOT NULL COMMENT '角色主键',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_sys_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `sys_roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sys_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `sys_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_roles`
--

LOCK TABLES `sys_user_roles` WRITE;
/*!40000 ALTER TABLE `sys_user_roles` DISABLE KEYS */;
INSERT INTO `sys_user_roles` VALUES (1,1,'2026-06-20 15:23:36.354'),(2,2,'2026-06-20 15:23:36.354'),(3,2,'2026-06-20 15:23:36.354'),(4,3,'2026-06-20 15:23:36.354'),(5,3,'2026-06-20 15:23:36.354'),(6,1,'2026-06-20 15:23:36.354'),(7,3,'2026-06-20 15:23:36.354'),(8,3,'2026-06-20 15:23:36.354'),(10,2,'2026-06-20 15:23:36.354');
/*!40000 ALTER TABLE `sys_user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_users`
--

DROP TABLE IF EXISTS `sys_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户内部主键',
  `account_no` varchar(64) NOT NULL COMMENT '登录账号：学号、教师工号或管理员账号',
  `role` enum('admin','teacher','student') NOT NULL COMMENT '用户角色',
  `password_hash` varchar(255) NOT NULL COMMENT '加密后的密码，禁止存储明文密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `gender` enum('male','female','unknown') NOT NULL DEFAULT 'unknown' COMMENT '性别',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(120) DEFAULT NULL COMMENT '邮箱地址',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像地址',
  `status` enum('active','disabled','deleted','pending') NOT NULL DEFAULT 'active' COMMENT '账号状态',
  `last_login_at` datetime(3) DEFAULT NULL COMMENT '最近登录时间',
  `login_count` int unsigned NOT NULL DEFAULT '0' COMMENT '成功登录总次数',
  `agreed_terms_at` datetime(3) DEFAULT NULL COMMENT '用户同意条款时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted_at` datetime(3) DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_account` (`role`,`account_no`),
  KEY `idx_role_status` (`role`,`status`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一用户账号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_users`
--

LOCK TABLES `sys_users` WRITE;
/*!40000 ALTER TABLE `sys_users` DISABLE KEYS */;
INSERT INTO `sys_users` VALUES (1,'admin001','admin','123123','系统管理员','unknown','13800000001','admin@scnu.example','/static/images/icons/system-icon.png','active','2026-07-02 09:33:41.037',63,'2026-01-01 00:00:00.000','2026-01-01 00:00:00.000','2026-07-02 09:33:41.037',NULL),(2,'T20230001','teacher','123123','张教授','male','13800138001','zhang@scnu.example','/static/images/icons/teacher-avatar.svg','active','2026-06-26 16:01:23.408',38,'2026-03-01 10:00:00.000','2026-03-01 10:00:00.000','2026-06-26 16:01:23.408',NULL),(3,'T20230002','teacher','123123','李老师','female','13800138002','li@scnu.example','/static/images/icons/teacher-avatar.svg','active','2026-06-10 16:20:00.000',9,'2026-03-02 10:00:00.000','2026-03-02 10:00:00.000','2026-06-10 16:20:00.000',NULL),(4,'S20230001','student','$2a$10$NgFhHKPOYhy5YCc8jQc77OqG1dWhVCSKnsjkALWdpoOogWMf94tOi','王同学','male','13900139001','wang.student@scnu.example','/static/images/icons/user-avatar.svg','active','2026-07-03 17:41:14.870',64,'2026-03-10 09:00:00.000','2026-03-10 09:00:00.000','2026-07-03 17:41:14.870',NULL),(5,'S20230002','student','123123','赵同学','female','13900139002','zhao.student@scnu.example','/static/images/icons/user-avatar.svg','disabled','2026-06-08 13:20:00.000',5,'2026-03-11 09:00:00.000','2026-03-11 09:00:00.000','2026-06-08 13:20:00.000',NULL),(6,'admin002','admin','123123','审核管理员','unknown','13800000002','reviewer@scnu.example','/static/images/icons/system-icon.png','active','2026-06-11 10:00:00.000',7,'2026-01-01 00:00:00.000','2026-01-01 00:00:00.000','2026-06-11 10:00:00.000',NULL),(7,'S1781415100885','student','$2a$10$GC2lz0Hljxx2ouzubcDav.2gXMbqKVGx33lhER7FWcYzsRz7.RVpu','测试用户','unknown',NULL,NULL,NULL,'active',NULL,0,NULL,'2026-06-14 13:31:41.128','2026-06-14 13:31:41.128',NULL),(8,'S99test01','student','$2a$10$GzqWEjrMFyILYL0kQbfcrO6OmObXvFllMLIzWOFFnuD.2UD.8PmkK','测试学生','male','13900000000',NULL,NULL,'active','2026-06-14 13:32:38.531',1,'2026-06-14 13:32:38.344','2026-06-14 13:32:38.344','2026-06-14 13:32:38.531',NULL),(9,'T1781415174880','teacher','$2a$10$vL83OIsKIxvXYqL3oWoKNuXFf60MYKnRX3YNRsKgcVCuSgGFcSohy','改名B','unknown','13700000000',NULL,NULL,'deleted',NULL,0,NULL,'2026-06-14 13:32:55.141','2026-06-14 13:32:55.536','2026-06-14 13:32:55.536'),(10,'T99test01','teacher','$2a$10$4.nWAAa4cnjobeCJte6EG.zFLW3CPzlNnieVP3nfgt02LvTEgw2gC','测试教师','female','13800000000',NULL,NULL,'active',NULL,0,NULL,'2026-06-16 15:19:37.466','2026-06-16 15:19:37.466',NULL);
/*!40000 ALTER TABLE `sys_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_settings`
--

DROP TABLE IF EXISTS `system_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_settings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '系统设置主键',
  `system_name` varchar(100) NOT NULL COMMENT '系统显示名称',
  `school_name` varchar(100) NOT NULL COMMENT '学校显示名称',
  `logo_url` varchar(500) DEFAULT NULL COMMENT '系统标识图片地址',
  `reservation_start_time` time NOT NULL COMMENT '每日预约开始时间',
  `reservation_end_time` time NOT NULL COMMENT '每日预约结束时间',
  `advance_days` int unsigned NOT NULL DEFAULT '3' COMMENT '允许提前预约的天数',
  `auto_approval` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用自动审批',
  `reservation_notification_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用预约通知',
  `approval_notification_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用审批通知',
  `reminder_notification_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用提醒通知',
  `updated_by` bigint unsigned DEFAULT NULL COMMENT '最后更新人管理员用户标识',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_updated_by` (`updated_by`),
  CONSTRAINT `fk_system_settings_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_system_settings_time` CHECK ((`reservation_end_time` > `reservation_start_time`))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局系统设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_settings`
--

LOCK TABLES `system_settings` WRITE;
/*!40000 ALTER TABLE `system_settings` DISABLE KEYS */;
INSERT INTO `system_settings` VALUES (1,'实验室预约管理系统','SCNU','/static/images/天空实验室.png','08:30:00','22:00:00',5,1,1,1,1,1,'2026-01-01 00:00:00.000','2026-06-14 13:31:41.363');
/*!40000 ALTER TABLE `system_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_profiles`
--

DROP TABLE IF EXISTS `teacher_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_profiles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '教师资料主键',
  `user_id` bigint unsigned NOT NULL COMMENT '关联用户表主键',
  `teacher_no` varchar(64) NOT NULL COMMENT '教师工号',
  `college` varchar(100) NOT NULL COMMENT '学院名称',
  `department` varchar(100) DEFAULT NULL COMMENT '系部名称',
  `position_title` varchar(50) DEFAULT NULL COMMENT '职务或职称',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`),
  CONSTRAINT `fk_teacher_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `sys_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师扩展资料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_profiles`
--

LOCK TABLES `teacher_profiles` WRITE;
/*!40000 ALTER TABLE `teacher_profiles` DISABLE KEYS */;
INSERT INTO `teacher_profiles` VALUES (1,2,'T20230001','计算机学院','软件工程系','教授','2026-03-01 10:00:00.000','2026-06-11 08:30:00.000'),(2,3,'T20230002','经济与管理学院','信息管理系','讲师','2026-03-02 10:00:00.000','2026-06-10 16:20:00.000'),(3,10,'T99test01','数学科学学院',NULL,NULL,'2026-06-16 15:19:37.524','2026-06-16 15:19:37.524');
/*!40000 ALTER TABLE `teacher_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_registration_applications`
--

DROP TABLE IF EXISTS `teacher_registration_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_registration_applications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '教师注册申请主键',
  `application_no` varchar(64) NOT NULL COMMENT '业务申请编号',
  `teacher_no` varchar(64) NOT NULL COMMENT '教师工号',
  `real_name` varchar(50) NOT NULL COMMENT '教师真实姓名',
  `gender` enum('male','female','unknown') NOT NULL DEFAULT 'unknown' COMMENT '性别',
  `college` varchar(100) DEFAULT NULL COMMENT '学院名称',
  `department` varchar(100) DEFAULT NULL COMMENT '系部名称',
  `position_title` varchar(50) DEFAULT NULL COMMENT '职务或职称',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `email` varchar(120) DEFAULT NULL COMMENT '邮箱地址',
  `password_hash` varchar(255) NOT NULL COMMENT '加密后的注册密码',
  `id_card_front_url` varchar(500) DEFAULT NULL COMMENT '身份证或证明材料正面图片地址',
  `id_card_back_url` varchar(500) DEFAULT NULL COMMENT '身份证或证明材料背面图片地址',
  `teacher_card_image_url` varchar(500) DEFAULT NULL COMMENT '教师证件图片地址',
  `status` enum('pending','approved','rejected') NOT NULL DEFAULT 'pending' COMMENT '审核状态',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因',
  `reviewer_user_id` bigint unsigned DEFAULT NULL COMMENT '管理员审核人用户标识',
  `applied_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '提交时间',
  `reviewed_at` datetime(3) DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_status_applied` (`status`,`applied_at`),
  KEY `idx_teacher_no` (`teacher_no`),
  KEY `idx_reviewer_user_id` (`reviewer_user_id`),
  CONSTRAINT `fk_teacher_registration_reviewer` FOREIGN KEY (`reviewer_user_id`) REFERENCES `sys_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师注册审核申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_registration_applications`
--

LOCK TABLES `teacher_registration_applications` WRITE;
/*!40000 ALTER TABLE `teacher_registration_applications` DISABLE KEYS */;
INSERT INTO `teacher_registration_applications` VALUES (1,'TRA20260611001','T20230003','陈老师','female','外国语学院','法语系','讲师','13700137003','chen@scnu.example','123123','/mock/id-card-front-chen.png','/mock/id-card-back-chen.png','/mock/teacher-card-chen.png','rejected','测试驳回',1,'2026-06-10 09:18:33.000','2026-06-14 13:31:41.659','2026-06-10 09:18:33.000','2026-06-14 13:31:41.659'),(2,'TRA20260611002','T20230004','刘教授','male','数学科学学院','应用数学系','教授','13700137004','liu@scnu.example','123123','/mock/id-card-front-liu.png','/mock/id-card-back-liu.png','/mock/teacher-card-liu.png','approved',NULL,6,'2026-06-09 14:35:22.000','2026-06-10 10:15:30.000','2026-06-09 14:35:22.000','2026-06-10 10:15:30.000'),(3,'TRA20260611003','T20230005','孙老师','male','物理学院','实验教学中心','实验师','13700137005','sun@scnu.example','123123','/mock/id-card-front-sun.png','/mock/id-card-back-sun.png','/mock/teacher-card-sun.png','rejected','教师证明材料不清晰，请重新上传。',6,'2026-06-08 16:42:18.000','2026-06-09 11:20:45.000','2026-06-08 16:42:18.000','2026-06-09 11:20:45.000'),(4,'TRA20260614133238862','T99test01','测试教师','female','数学科学学院',NULL,NULL,'13800000000',NULL,'$2a$10$4.nWAAa4cnjobeCJte6EG.zFLW3CPzlNnieVP3nfgt02LvTEgw2gC',NULL,NULL,NULL,'approved',NULL,1,'2026-06-14 13:32:38.682','2026-06-16 15:19:37.574','2026-06-14 13:32:38.682','2026-06-16 15:19:37.574');
/*!40000 ALTER TABLE `teacher_registration_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'company_level'
--
--
-- WARNING: can't read the INFORMATION_SCHEMA.libraries table. It's most probably an old server 8.4.3-SQLPub-0.0.1.
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 23:02:37
