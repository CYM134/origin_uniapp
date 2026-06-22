-- =====================================================================
-- V1__portal_app.sql  应用中心相关表
-- 校园综合服务平台 - 应用分类 / 应用 / 用户收藏 / 应用访问日志
-- 兼容策略：全部使用 CREATE TABLE IF NOT EXISTS，可重复执行；不修改任何旧表。
-- 字段/索引风格对齐现有 schema.sql（BIGINT UNSIGNED 主键、DATETIME(3)、utf8mb4）。
-- =====================================================================

-- 应用分类表 -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS portal_app_category (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(50)     NOT NULL                  COMMENT '分类名称',
  category_code VARCHAR(50)     NOT NULL                  COMMENT '分类编码，唯一',
  icon          VARCHAR(50)     DEFAULT NULL              COMMENT '分类图标标识',
  sort_order    INT             NOT NULL DEFAULT 0        COMMENT '排序号，升序',
  status        TINYINT(1)      NOT NULL DEFAULT 1        COMMENT '状态：1 启用，0 停用',
  remark        VARCHAR(255)    DEFAULT NULL              COMMENT '备注',
  created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_code (category_code),
  KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-应用分类';

-- 应用表 ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS portal_app (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  app_name      VARCHAR(80)     NOT NULL                  COMMENT '应用名称',
  app_code      VARCHAR(64)     NOT NULL                  COMMENT '应用编码，唯一',
  category_id   BIGINT UNSIGNED DEFAULT NULL              COMMENT '应用分类 id',
  icon          VARCHAR(50)     DEFAULT NULL              COMMENT '应用图标标识',
  description   VARCHAR(255)    DEFAULT NULL              COMMENT '应用描述',
  route_path    VARCHAR(200)    DEFAULT NULL              COMMENT '内部路由（uni-app 页面路径或逻辑路由）',
  external_url  VARCHAR(500)    DEFAULT NULL              COMMENT '外部链接',
  open_type     VARCHAR(20)     NOT NULL DEFAULT 'internal' COMMENT '打开方式：internal 内部路由，external 外部链接',
  visible_roles VARCHAR(100)    NOT NULL DEFAULT 'admin,teacher,student' COMMENT '可见角色编码，逗号分隔（兼容 sys_users.role 枚举）',
  is_hot        TINYINT(1)      NOT NULL DEFAULT 0        COMMENT '是否热门',
  is_new        TINYINT(1)      NOT NULL DEFAULT 0        COMMENT '是否最新',
  status        TINYINT(1)      NOT NULL DEFAULT 1        COMMENT '状态：1 启用，0 停用',
  sort_order    INT             NOT NULL DEFAULT 0        COMMENT '排序号，升序',
  visit_count   INT UNSIGNED    NOT NULL DEFAULT 0        COMMENT '累计访问次数',
  created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_code (app_code),
  KEY idx_category (category_id),
  KEY idx_status_sort (status, sort_order),
  KEY idx_hot (is_hot),
  KEY idx_new (is_new)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-应用';

-- 用户应用收藏表 -------------------------------------------------------
CREATE TABLE IF NOT EXISTS portal_user_app_favorite (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL                    COMMENT '用户 id（sys_users.id）',
  app_id     BIGINT UNSIGNED NOT NULL                    COMMENT '应用 id（portal_app.id）',
  created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '收藏时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_app (user_id, app_id),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-用户收藏';

-- 应用访问日志表 -------------------------------------------------------
CREATE TABLE IF NOT EXISTS portal_app_visit_log (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED DEFAULT NULL               COMMENT '访问用户 id',
  app_id      BIGINT UNSIGNED NOT NULL                   COMMENT '应用 id',
  visit_time  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '访问时间',
  client_type VARCHAR(20)     DEFAULT NULL               COMMENT '客户端类型：pc / miniapp / mobile',
  ip_address  VARCHAR(64)     DEFAULT NULL               COMMENT '访问 IP',
  PRIMARY KEY (id),
  KEY idx_app_time (app_id, visit_time),
  KEY idx_user_time (user_id, visit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用中心-访问日志';
