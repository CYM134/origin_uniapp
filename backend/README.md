# 后端说明

本目录是校园综合服务平台后端，基于 Java 17 + Spring Boot 3.3.0 构建，提供登录鉴权、实验室预约、综合服务门户、通知公告、校园资讯、任务中心、日程、AI 问答、管理端工作台等 REST API。

完整从零启动流程见：

```text
../deploy/README.md
```

## 技术栈

- Java 17
- Spring Boot Web / Security / Validation / JDBC
- JWT + Redis 会话控制
- MySQL 8
- Redis 7
- MyBatis 试点
- springdoc-openapi / Swagger UI
- OpenAI 兼容 AI 模型适配层

## 目录

| 路径 | 说明 |
| --- | --- |
| `src/main/java` | 后端源码 |
| `src/main/resources/application.yml` | 公共配置 |
| `src/main/resources/application-dev.yml` | 本地开发配置，默认启用 |
| `src/main/resources/application-prod.yml` | Docker/生产配置，通过环境变量注入 |
| `docs/schema.sql` | 基础表结构和演示数据 |
| `docs/sql/` | 校园综合服务平台增量 SQL |
| `pom.xml` | Maven 配置 |

## 推荐运行方式

在仓库上一级 `origin_uniapp` 目录使用 Docker Compose：

```powershell
Copy-Item .env.example .env
docker compose up -d --build
```

访问：

```text
http://localhost:8080/api/health
http://localhost/swagger-ui.html
```

## 本地开发运行

如果只想本地启动后端进程，可以用 Docker 启动 MySQL/Redis：

```powershell
cd ..
docker compose up -d mysql redis
```

再启动 Spring Boot：

```powershell
cd backend
mvn spring-boot:run
```

默认 profile 是 `dev`，会读取 `application-dev.yml`，连接本机 Docker 暴露的 MySQL/Redis。Docker Compose 启动后端时会设置 `SPRING_PROFILES_ACTIVE=prod`，读取 `application-prod.yml`。

测试：

```powershell
mvn test
```

## 默认账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin001` | `123123` |
| 教师 | `T20230001` | `123123` |
| 学生 | `S20230001` | `123123` |

## 重要环境变量

| 变量 | 说明 |
| --- | --- |
| `DB_URL` | MySQL JDBC 地址 |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL 账号密码 |
| `REDIS_HOST` / `REDIS_PORT` | Redis 地址 |
| `SPRING_PROFILES_ACTIVE` | Spring Boot profile，Docker 默认 `prod`，本地默认 `dev` |
| `JWT_SECRET` | JWT 签名密钥，生产环境必须更换 |
| `REDIS_AUTH_SESSION_ENABLED` | 是否启用 Redis 会话校验 |
| `OPENAPI_ENABLED` / `SWAGGER_UI_ENABLED` | 是否启用接口文档 |
| `AI_ENABLED` / `AI_API_URL` / `AI_API_KEY` / `AI_MODEL` | AI 模型配置 |

## 已接入的 MyBatis Mapper

- `portal.dashboard.AdminDashboardMapper`：管理员统计。
- `reservation.ReservationMapper`：预约列表、审批列表、预约详情查询。
- `ai.AiContextMapper`：AI 回答前的预约、待办、消息、日程、公告上下文查询。
- `portal.notice.NoticeMapper`：通知公告门户/管理端列表与详情查询。
- `portal.news.NewsMapper`：校园资讯分类、门户/管理端列表与详情查询。

写操作仍主要保留在 `JdbcTemplate`，便于控制事务、乐观状态流转和生成主键。
