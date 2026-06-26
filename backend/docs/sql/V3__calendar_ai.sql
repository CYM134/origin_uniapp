-- =====================================================================
-- V3__calendar_ai.sql  日历日程 / AI 助手相关表（P2 可选模块）
-- 兼容策略：CREATE TABLE IF NOT EXISTS，可重复执行；不修改任何旧表。
-- =====================================================================

SET NAMES utf8mb4;

-- 日历事件表 -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS calendar_event (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id               BIGINT UNSIGNED NOT NULL           COMMENT '所属用户 id（个人日程）',
  title                 VARCHAR(150)    NOT NULL           COMMENT '事件标题',
  event_type            VARCHAR(30)     NOT NULL DEFAULT 'custom' COMMENT '类型：reservation/approval/school/custom',
  start_time            DATETIME(3)     NOT NULL           COMMENT '开始时间',
  end_time              DATETIME(3)     DEFAULT NULL       COMMENT '结束时间',
  location              VARCHAR(150)    DEFAULT NULL       COMMENT '地点',
  remark                VARCHAR(500)    DEFAULT NULL       COMMENT '备注',
  related_business_type VARCHAR(50)     DEFAULT NULL       COMMENT '关联业务类型，如 reservation',
  related_business_id   BIGINT UNSIGNED DEFAULT NULL       COMMENT '关联业务 id',
  created_at            DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at            DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_user_start (user_id, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日历事件';

-- AI 会话表 ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_conversation (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL                     COMMENT '所属用户 id',
  title      VARCHAR(150)    DEFAULT NULL                 COMMENT '会话标题（取首条提问摘要）',
  created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_user_updated (user_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 助手会话';

-- AI 消息表 ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_message (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id BIGINT UNSIGNED NOT NULL                COMMENT '会话 id',
  role            VARCHAR(20)     NOT NULL                COMMENT '角色：user/assistant/system',
  content         MEDIUMTEXT      NOT NULL                COMMENT '消息内容',
  created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_conversation_created (conversation_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 助手消息';
