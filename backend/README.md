# Backend
`origin_uniapp` 后端项目，使用 Java 语言构建，应用 Spring Boot 框架。
系统利用 Spring Boot 构建 RESTful API 接口，负责处理前端小程序的各类请求，实现业务逻辑的封装和执行。
在数据存储层面，选用 MySQL 关系型数据库用于数据存储。

## 校园综合服务平台扩展（2026-06 新增）

在原「实验室预约管理系统」基础上**增量扩展**为校园综合服务平台，遵循“不改旧表、不重写登录、不删除实验室预约功能、不新增 super_admin”的原则（admin 在第一阶段兼任超级管理员）。完整说明见仓库根目录 [`校园综合服务平台-交付说明.md`](../校园综合服务平台-交付说明.md)。

### 新增后端模块（位于 `com.companyleveltraining.backend` 下）
- `portal.app` 应用中心：应用分类、按角色可见应用、收藏、访问统计，管理员应用增删改查/启停/排序
- `portal.home` 师生门户首页聚合
- `portal.notice` 通知公告（门户查看/已读 + 管理员发布/置顶/上下线）
- `portal.news` 校园资讯（门户 + 管理员）
- `portal.task` 任务中心（聚合现有预约审批数据，未新建 task 表）
- `portal.menu` 菜单与按钮权限
- `portal.dashboard` 管理员综合工作台
- `portal.calendar` 我的日历（个人日程 + 预约派生事件）
- `ai` AI 校园助手（OpenAI 兼容接口，密钥经环境变量注入，未配置时自动降级为内置规则问答）
- 复用并增强：预约**提交时即生成站内消息**；新增 `GET /api/laboratories/{id}/available-times`

### 新增接口前缀
- `/api/portal/*`：门户首页、应用中心、通知公告、校园资讯、任务中心、日历
- `/api/admin/portal/*`、`/api/admin/notices/*`、`/api/admin/news/*`：管理端
- `/api/ai/*`：AI 助手
- `/api/auth/menus`、`/api/auth/permissions`：菜单 / 按钮权限

### 新增数据库脚本（`docs/sql/`，均可重复执行、不改旧表）
- `V1__portal_app.sql`、`V2__notice_news.sql`、`V3__calendar_ai.sql`、`V4__init_portal_data.sql`
- 共新增 11 张表（应用中心 4 + 公告/资讯 4 + 日历/AI 3），并初始化 9 分类 + 15 应用 + 演示公告/资讯/日程

### 新增前端（uni-app，`../origin_uniapp`）
- `api/portal.js` + 14 个页面：门户首页、应用中心、通知/资讯列表与详情、消息中心、任务中心、我的日历、AI 助手、管理员工作台及应用/通知/资讯管理；并在原 student/teacher/admin 工作台增加“综合服务门户/工作台”入口。

### AI 助手配置（可选）
通过环境变量启用真实大模型，否则走内置规则问答：

```text
AI_ENABLED=true
AI_API_URL=<OpenAI 兼容的 chat/completions 地址>
AI_API_KEY=<密钥>
AI_MODEL=<模型名，默认 gpt-3.5-turbo>
```

## 项目结构

- `pom.xml`：Maven 项目定义
- `src/main/java`：Spring Boot 启动类与接口代码
- `src/main/resources/application.yml`：数据库与服务配置
- `docs/schema.sql`：数据库表结构脚本

## 运行环境要求

启动本项目前，必须先安装以下环境：

- `JDK 22`
- `Apache Maven 3.9.x` 或更高版本
- 可访问外网的网络环境
  - 首次启动时 Maven 需要从中央仓库下载依赖
- 可访问 MySQL 服务器的网络环境

建议先执行以下命令确认环境：

```powershell
java -version
mvn -version
```

## 必须下载的依赖包

本项目使用 Maven 管理依赖。首次启动时，Maven 会自动下载以下核心依赖，无需手动逐个下载：

- `org.springframework.boot:spring-boot-starter-web`
  - 提供 Spring MVC、REST API、内嵌 Tomcat
- `org.springframework.boot:spring-boot-starter-jdbc`
  - 提供 JDBC、数据源配置、`JdbcTemplate`
- `org.springframework.boot:spring-boot-starter-validation`
  - 提供参数校验能力
- `com.mysql:mysql-connector-j`
  - MySQL JDBC 驱动，连接 MySQL 必需
- `org.springframework.boot:spring-boot-starter-test`
  - 测试依赖，仅测试时使用
- `org.springframework.boot:spring-boot-maven-plugin`
  - 支持 `mvn spring-boot:run` 启动项目

## 数据库连接配置

当前默认连接配置已写入 `application.yml`，目标数据库如下：

- Host: `mysql6.sqlpub.com`
- Port: `3311`
- Database: `company_level`
- Username: `super_admin666`

可通过环境变量覆盖默认值：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`

默认 JDBC 连接地址：

```text
jdbc:mysql://mysql6.sqlpub.com:3311/company_level?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

## 数据库准备

启动项目之前，先确认数据库已经完成以下准备：

```powershell
mysql -h mysql6.sqlpub.com -P 3311 -u super_admin666 -p company_level < .\docs\schema.sql
```

校园综合服务平台扩展表需在 `schema.sql` 之后依次导入（均可重复执行）：

```powershell
mysql -h mysql6.sqlpub.com -P 3311 -u super_admin666 -p company_level < .\docs\sql\V1__portal_app.sql
mysql -h mysql6.sqlpub.com -P 3311 -u super_admin666 -p company_level < .\docs\sql\V2__notice_news.sql
mysql -h mysql6.sqlpub.com -P 3311 -u super_admin666 -p company_level < .\docs\sql\V3__calendar_ai.sql
mysql -h mysql6.sqlpub.com -P 3311 -u super_admin666 -p company_level < .\docs\sql\V4__init_portal_data.sql
```

## 启动项目

注意：`pom.xml` 在 `backend` 目录内，必须进入该目录再执行 Maven 命令。

### 1. 进入后端目录

```powershell
cd D:\python\companyLevelTraining\backend
```

### 2. 启动 Spring Boot 项目

```powershell
mvn spring-boot:run
```


### 3. 启动成功的标志

控制台出现类似以下内容，说明服务已经启动：

```text
Started BackendApplication in ...
Tomcat started on port(s): 8080
```

### 4. 自定义启动参数

可以通过环境变量覆盖默认数据库配置。例如 PowerShell：

```powershell
$env:DB_URL="jdbc:mysql://mysql6.sqlpub.com:3311/company_level?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="super_admin666"
$env:DB_PASSWORD="03cCT9fozmtt7BcT"
$env:SERVER_PORT="8080"
mvn spring-boot:run
```

## 启动后如何测试

项目启动后，可通过以下接口验证服务和数据库连接：

### 1. 检查服务是否启动

请求：

```text
GET http://localhost:8080/api/health
```

返回示例：

```json
{
  "status": "UP",
  "service": "backend",
  "timestamp": "2026-06-11T14:00:00"
}
```

### 2. 检查数据库是否连通

请求：

```text
GET http://localhost:8080/api/health/db
```

返回示例：

```json
{
  "status": "UP",
  "database": "company_level",
  "host": "xxx",
  "port": 3311,
  "serverTime": "2026-06-11T14:00:00.000"
}
```

如果该接口能正常返回，说明：

- Spring Boot 服务已启动
- MySQL 驱动已加载成功
- 数据源配置正确
- 应用可以正常连接远程数据库

## 常用命令

```powershell
cd D:\python\companyLevelTraining\backend
mvn dependency:resolve
mvn spring-boot:run
mvn test
```

## 常见问题

### 1. `No plugin found for prefix 'spring'`

原因：执行了错误命令 `mvn spring:run`。

正确命令：

```powershell
mvn spring-boot:run
```

### 2. `mvn` 不是内部或外部命令

原因：Maven 未安装，或未正确配置到 `PATH`。

处理方式：

- 安装 Apache Maven
- 将 Maven 的 `bin` 目录加入系统 `PATH`
- 重新打开 PowerShell 后再执行 `mvn -version`

### 3. `Communications link failure`

原因通常是：

- 数据库地址或端口错误
- 本机网络无法访问远程 MySQL
- 数据库服务器限制了外部访问

### 4. `Access denied for user`

原因通常是：

- 用户名错误
- 密码错误
- 当前账号没有目标数据库权限

### 5. `Unknown database 'company_level'`

原因：数据库尚未创建，或库名写错。

## 当前已提供的接口

- `GET /api/health`
- `GET /api/health/db`

其中 `/api/health/db` 会直接访问 MySQL，可用于验证数据库是否连接成功。
