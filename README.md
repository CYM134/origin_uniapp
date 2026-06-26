# 校园综合服务平台

本项目是在原实验室预约管理系统基础上扩展的校园综合服务平台，包含 uni-app 前端、Spring Boot 后端、MySQL、Redis、Nginx、OpenAPI/Swagger、AI 助手和 MyBatis 试点。

## 推荐启动方式

第一次配置环境，推荐走 Docker Compose：

```powershell
Copy-Item .env.example .env
docker compose up -d --build
```

启动后访问：

```text
Nginx:        http://localhost
后端接口:     http://localhost:8080
Swagger UI:  http://localhost/swagger-ui.html
健康检查:     http://localhost:8080/api/health
```

完整的从零安装、启动、前端 HBuilderX 运行、常见问题处理见：

```text
deploy/README.md
```

## 默认演示账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin001` | `123123` |
| 教师 | `T20230001` | `123123` |
| 学生 | `S20230001` | `123123` |

## 目录说明

| 路径 | 说明 |
| --- | --- |
| `origin_uniapp/` | uni-app 前端工程，使用 HBuilderX 打开 |
| `backend/` | Spring Boot 后端 |
| `deploy/` | Nginx 配置、占位前端页面、部署说明 |
| `docker-compose.yml` | MySQL、Redis、后端、Nginx 编排 |
| `.env.example` | Docker Compose 环境变量示例 |
| `校园综合服务平台-交付说明.md` | 功能交付说明 |

## 当前技术栈

- 前端：uni-app、Vue 3、TypeScript/Less
- 后端：Java 17、Spring Boot 3.3、Spring Web、Spring Security、JWT
- 数据：MySQL 8、Redis 7、JdbcTemplate、MyBatis 试点
- 部署：Docker Compose、Nginx
- 接口文档：springdoc-openapi / Swagger UI
- AI：OpenAI 兼容 chat/completions 适配层，未配置模型时使用内置业务上下文问答兜底

## 图中技术栈覆盖情况

根据课程截图中能辨认出的技术栈，目前项目覆盖情况如下：

| 技术栈 | 覆盖情况 | 项目里的体现 |
| --- | --- | --- |
| Linux / 服务器运行环境 | 部分覆盖 | Docker 容器使用 Linux 镜像；尚未编写云服务器手工部署脚本 |
| Maven | 已覆盖 | 后端 `pom.xml`，Docker 构建阶段使用 Maven 打包 |
| Spring Boot | 已覆盖 | 后端主框架，REST API、配置、容器启动均基于 Spring Boot |
| Spring Security / JWT | 已覆盖 | 登录鉴权、JWT、Redis 会话控制 |
| MySQL | 已覆盖 | 业务主数据库，Docker Compose 自动初始化 |
| Redis | 已覆盖 | 工作台缓存、JWT 会话控制 |
| MyBatis | 已覆盖 | 管理统计、预约/审批查询、AI 上下文、通知公告、校园资讯读查询 |
| Nginx | 已覆盖 | 静态前端托管、`/api` 反向代理、Swagger 代理 |
| Docker / Docker Compose | 已覆盖 | MySQL、Redis、后端、Nginx 一键编排 |
| Swagger / OpenAPI | 已覆盖 | `springdoc-openapi`，访问 `/swagger-ui.html` |
| AI / DeepSeek 类大模型 | 部分覆盖 | 已有 OpenAI 兼容适配层，可接 DeepSeek；当前默认不配置真实密钥，走业务上下文兜底 |
| Spring AI | 未覆盖 | 当前是自研轻量模型适配层，没有引入 Spring AI starter |
| Spring Cloud Alibaba / Nacos / Gateway / OpenFeign / Sentinel | 未覆盖 | 当前是单体应用，没有拆微服务 |
| 消息队列类组件 | 未覆盖 | 当前站内通知直接写库，没有 RabbitMQ/RocketMQ/Kafka |
| MinIO / 对象存储 | 未覆盖 | 当前图片/附件仍是 URL 字段，没有对象存储服务 |
| JMeter / Postman 等测试工具 | 未内置 | 可用于外部压测/接口测试，但不是项目运行依赖 |
