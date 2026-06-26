-- =====================================================================
-- V2__notice_news.sql  通知公告 / 校园资讯相关表
-- 说明：notice = 面向群体的"公告"，与现有 notifications（面向个人的"消息"）区分。
-- 兼容策略：CREATE TABLE IF NOT EXISTS，可重复执行；不修改任何旧表。
-- =====================================================================

SET NAMES utf8mb4;

-- 通知公告表 -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS notice (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  notice_no      VARCHAR(64)     NOT NULL                 COMMENT '公告业务编号，唯一',
  title          VARCHAR(150)    NOT NULL                 COMMENT '标题',
  content        MEDIUMTEXT      NOT NULL                 COMMENT '正文',
  notice_type    VARCHAR(30)     NOT NULL DEFAULT 'system' COMMENT '类型：system/activity/maintenance/policy 等',
  publish_scope  VARCHAR(20)     NOT NULL DEFAULT 'all'   COMMENT '发布范围：all 全体，role 指定角色',
  target_roles   VARCHAR(100)    DEFAULT NULL             COMMENT '目标角色编码，逗号分隔（scope=role 时生效）',
  is_top         TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否置顶',
  status         VARCHAR(20)     NOT NULL DEFAULT 'draft' COMMENT '状态：draft 草稿，published 已发布，offline 已下线',
  publish_time   DATETIME(3)     DEFAULT NULL             COMMENT '发布时间',
  publisher_id   BIGINT UNSIGNED DEFAULT NULL             COMMENT '发布人 id',
  publisher_name VARCHAR(50)     DEFAULT NULL             COMMENT '发布人姓名快照',
  view_count     INT UNSIGNED    NOT NULL DEFAULT 0       COMMENT '浏览次数',
  created_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at     DATETIME(3)     DEFAULT NULL             COMMENT '逻辑删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_notice_no (notice_no),
  KEY idx_status_publish (status, publish_time),
  KEY idx_top_publish (is_top, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告';

-- 通知公告阅读记录表 ---------------------------------------------------
CREATE TABLE IF NOT EXISTS notice_read (
  id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  notice_id BIGINT UNSIGNED NOT NULL                     COMMENT '公告 id',
  user_id   BIGINT UNSIGNED NOT NULL                     COMMENT '用户 id',
  read_time DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '阅读时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_notice_user (notice_id, user_id),
  KEY idx_user_time (user_id, read_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告阅读记录';

-- 资讯分类表 -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS news_category (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(50)     NOT NULL                  COMMENT '分类名称',
  category_code VARCHAR(50)     NOT NULL                  COMMENT '分类编码，唯一',
  sort_order    INT             NOT NULL DEFAULT 0        COMMENT '排序号',
  status        TINYINT(1)      NOT NULL DEFAULT 1        COMMENT '状态：1 启用，0 停用',
  created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_news_category_code (category_code),
  KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园资讯分类';

-- 资讯表 ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS news (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  news_no       VARCHAR(64)     NOT NULL                  COMMENT '资讯业务编号，唯一',
  title         VARCHAR(150)    NOT NULL                  COMMENT '标题',
  summary       VARCHAR(300)    DEFAULT NULL              COMMENT '摘要',
  content       MEDIUMTEXT      DEFAULT NULL              COMMENT '正文',
  category_id   BIGINT UNSIGNED DEFAULT NULL              COMMENT '资讯分类 id',
  category_code VARCHAR(50)     DEFAULT NULL              COMMENT '资讯分类编码快照',
  cover_image   VARCHAR(500)    DEFAULT NULL              COMMENT '封面图',
  author        VARCHAR(50)     DEFAULT NULL              COMMENT '作者/来源',
  status        VARCHAR(20)     NOT NULL DEFAULT 'draft'  COMMENT '状态：draft 草稿，published 已发布，offline 已下线',
  is_top        TINYINT(1)      NOT NULL DEFAULT 0        COMMENT '是否置顶',
  publish_time  DATETIME(3)     DEFAULT NULL              COMMENT '发布时间',
  publisher_id  BIGINT UNSIGNED DEFAULT NULL             COMMENT '发布人 id',
  view_count    INT UNSIGNED    NOT NULL DEFAULT 0        COMMENT '浏览次数',
  created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at    DATETIME(3)     DEFAULT NULL              COMMENT '逻辑删除时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_news_no (news_no),
  KEY idx_status_publish (status, publish_time),
  KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园资讯';
