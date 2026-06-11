# Backend
`origin_uniapp` 后端项目，使用 Java 语言构建，应用 Spring Boot 框架。
系统利用 Spring Boot 构建 RESTful API 接口，负责处理前端小程序的各类请求，实现业务逻辑的封装和执行。
在数据存储层面，选用 MySQL 关系型数据库用于数据存储。

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
