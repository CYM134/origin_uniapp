# 从零开始运行校园综合服务平台

本文面向完全没有本项目运行环境的人。推荐先使用 Docker Compose 启动整套后端环境，因为它会自动准备 MySQL、Redis、Spring Boot 后端和 Nginx，不要求本机先安装 Java、Maven、MySQL 或 Redis。

## 1. 先装哪些软件

必须安装：

| 软件 | 用途 | 验证命令 |
| --- | --- | --- |
| Docker Desktop | 运行 MySQL、Redis、后端、Nginx | `docker version` |
| Docker Compose | 编排多个容器，Docker Desktop 通常已自带 | `docker compose version` |
| 浏览器 | 打开前端页面和 Swagger | 无 |

可选安装：

| 软件 | 什么时候需要 |
| --- | --- |
| Git | 需要从仓库 clone 代码时 |
| HBuilderX | 需要运行或构建 uni-app 前端时 |
| Java 17 + Maven 3.9.x | 需要不通过 Docker、直接本地开发后端时 |
| MySQL 客户端 | 需要手动连接数据库排查数据时 |

Windows 上如果 `docker version` 报无法连接 Docker API，通常是 Docker Desktop 没启动，或没有切到 Linux containers / WSL2 后端。

## 2. 获取代码

如果是 Git clone：

```powershell
cd E:\Github
git clone <你的仓库地址> git_origin_uniapp
cd git_origin_uniapp\origin_uniapp
```

如果是压缩包，解压后进入包含 `docker-compose.yml` 的目录即可。

后续命令都默认在这个目录执行：

```text
origin_uniapp/
```

也就是能看到这些文件的位置：

```text
docker-compose.yml
Dockerfile
.env.example
backend/
origin_uniapp/
deploy/
```

## 3. 创建环境变量文件

第一次运行先复制示例配置：

```powershell
Copy-Item .env.example .env
```

Docker Compose 默认使用 Spring Boot `prod` 配置：

```text
SPRING_PROFILES_ACTIVE=prod
```

不通过 Docker、本地直接运行后端时默认使用 `dev` 配置，不需要额外设置 profile。

默认端口：

| 服务 | 地址 |
| --- | --- |
| Nginx | `http://localhost` |
| 后端 | `http://localhost:8080` |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |

如果端口被占用，修改 `.env`：

```text
NGINX_PORT=8088
BACKEND_PORT=18080
MYSQL_PORT=13306
REDIS_PORT=16379
```

## 4. 一键启动后端环境

```powershell
docker compose up -d --build
```

第一次构建会下载 MySQL、Redis、Nginx、Maven、JDK 镜像和 Maven 依赖，时间会比较久。完成后查看容器状态：

```powershell
docker compose ps
```

正常会看到：

```text
campus-mysql
campus-redis
campus-backend
campus-nginx
```

## 5. 验证是否启动成功

健康检查：

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/health/db
```

浏览器打开：

```text
http://localhost
http://localhost/swagger-ui.html
http://localhost/v3/api-docs
```

Swagger 调试受保护接口时，先调用 `/api/auth/login` 获取 `accessToken`，再点 Swagger UI 右上角 `Authorize`，填：

```text
Bearer <accessToken>
```

## 6. 默认演示账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin001` | `123123` |
| 教师 | `T20230001` | `123123` |
| 学生 | `S20230001` | `123123` |

登录接口示例：

```powershell
$body = @{ accountNo = 'admin001'; password = '123123'; role = 'admin' } | ConvertTo-Json
Invoke-RestMethod http://localhost/api/auth/login -Method Post -ContentType 'application/json' -Body $body
```

## 7. 前端怎么运行

### 开发调试：HBuilderX 运行到浏览器

1. 打开 HBuilderX。
2. 选择“文件 -> 导入 -> 从本地目录导入”。
3. 选择前端目录：

```text
origin_uniapp/origin_uniapp
```

4. 先确保 Docker 后端已启动。
5. 在 HBuilderX 中选择“运行 -> 运行到浏览器 -> Chrome”。

前端默认请求：

```text
http://localhost:8080
```

这个地址来自：

```text
origin_uniapp/api/request.js
```

### 正式 H5：让 Nginx 托管构建产物

在 HBuilderX 中执行“发行 -> 网站 H5”，构建后通常生成：

```text
origin_uniapp/unpackage/dist/build/h5
```

然后把 `.env` 里的：

```text
NGINX_HTML_DIR=./deploy/html
```

改成：

```text
NGINX_HTML_DIR=./origin_uniapp/unpackage/dist/build/h5
```

重启 Nginx：

```powershell
docker compose up -d --force-recreate nginx
```

之后访问：

```text
http://localhost
```

## 8. 常用 Docker 命令

查看状态：

```powershell
docker compose ps
```

查看后端日志：

```powershell
docker logs -f campus-backend
```

重启后端：

```powershell
docker compose up -d --force-recreate backend
```

重新构建后端：

```powershell
docker compose build backend
docker compose up -d --force-recreate backend
```

停止服务但保留数据库数据：

```powershell
docker compose down
```

清空数据库并重新初始化：

```powershell
docker compose down -v
docker compose up -d --build
```

注意：`down -v` 会删除 MySQL/Redis 数据卷，演示数据会按 SQL 脚本重新导入。

## 9. 数据库初始化说明

Docker 第一次创建 `mysql-data` 数据卷时会自动执行：

```text
backend/docs/schema.sql
backend/docs/sql/V1__portal_app.sql
backend/docs/sql/V2__notice_news.sql
backend/docs/sql/V3__calendar_ai.sql
backend/docs/sql/V4__init_portal_data.sql
backend/docs/sql/V5__fix_portal_seed_encoding.sql
```

如果你修改了初始化 SQL，但容器里旧数据还在，MySQL 不会自动重跑脚本。需要执行：

```powershell
docker compose down -v
docker compose up -d --build
```

## 10. Redis 用途

Redis 当前用于两类功能：

| 功能 | 说明 |
| --- | --- |
| 管理员工作台缓存 | 缓存 `portal:admin:dashboard:snapshot`，默认 30 秒 |
| JWT 会话控制 | 登录写入 `auth:token:{jti}`，退出登录、改密码、禁用账号会让会话失效 |

可在 `.env` 中临时关闭会话校验：

```text
REDIS_AUTH_SESSION_ENABLED=false
```

## 11. AI 助手配置

AI 模块已拆成 OpenAI 兼容模型适配层。默认不接真实模型：

```text
AI_ENABLED=false
AI_API_URL=
AI_API_KEY=
AI_MODEL=gpt-3.5-turbo
AI_REQUIRE_API_KEY=true
AI_TIMEOUT_SECONDS=20
```

未配置模型时，`/api/ai/chat` 仍会按当前用户角色查询预约、通知、日程、待办等业务上下文，并使用内置规则兜底回答。

接入 DeepSeek 等 OpenAI 兼容服务时，修改 `.env`：

```text
AI_ENABLED=true
AI_API_URL=<chat/completions 地址>
AI_API_KEY=<密钥>
AI_MODEL=<模型名>
```

然后重启后端：

```powershell
docker compose up -d --force-recreate backend
```

## 12. MyBatis 试点

后端已接入 MyBatis：

| Mapper | 作用 |
| --- | --- |
| `AdminDashboardMapper` | 管理员首页/综合服务工作台统计 |
| `ReservationMapper` | 预约列表、审批列表、预约详情查询 |
| `AiContextMapper` | AI 回答前所需的预约、待办、消息、日程、公告上下文摘要查询 |
| `NoticeMapper` | 通知公告门户/管理端列表与详情查询 |
| `NewsMapper` | 校园资讯分类、门户/管理端列表与详情查询 |

项目仍保留 `JdbcTemplate`，尤其是创建预约、审核、取消、完成等写操作，避免一次性大迁移带来风险。

## 13. 本地开发后端，不用 Docker 构建后端

如果你想本地直接运行 Spring Boot：

1. 安装 Java 17。
2. 安装 Maven 3.9.x。
3. 仍然可以用 Docker 只启动 MySQL/Redis：

```powershell
docker compose up -d mysql redis
```

4. 直接启动。后端默认使用 `application-dev.yml`，连接 `localhost:3306` 的 `campus_service` 和 `localhost:6379` 的 Redis：

```powershell
cd backend
mvn spring-boot:run
```

如果需要临时指定配置，也可以手动设置：

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```

生产/容器环境使用 `application-prod.yml`，数据库、Redis、JWT 等信息都从环境变量注入，避免把真实密码写进配置文件。

## 14. 常见问题

### Docker 报无法连接 Docker API

先打开 Docker Desktop，等左下角状态变成运行中。Windows 上建议使用 Linux containers / WSL2 后端。

### 端口 80、8080、3306 或 6379 被占用

修改 `.env` 中的 `NGINX_PORT`、`BACKEND_PORT`、`MYSQL_PORT`、`REDIS_PORT`，然后重新执行：

```powershell
docker compose up -d
```

### 页面显示中文乱码

优先确认数据库初始化 SQL 是通过 Docker 自动导入的。如果曾经用 PowerShell 管道手动导入中文 SQL，可能写入过乱码数据。最简单的处理：

```powershell
docker compose down -v
docker compose up -d --build
```

### 改了 SQL 但数据没变化

MySQL 初始化脚本只在空数据卷首次启动时执行。需要清空数据卷：

```powershell
docker compose down -v
docker compose up -d --build
```

### Swagger 能打开，但接口 401

先调用 `/api/auth/login` 登录，把返回的 `accessToken` 按 `Bearer <accessToken>` 填入 Swagger UI 的 `Authorize`。

### HBuilderX 前端请求失败

先确认：

```text
http://localhost:8080/api/health
```

能正常返回。如果后端端口改过，需要同步修改前端保存的 `apiBaseUrl`，或清理浏览器/应用缓存后重新运行。
