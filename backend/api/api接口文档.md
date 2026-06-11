# 后端 API 接口文档

本文档仅描述当前已经在 `backend` 中实现的接口。
当前已实现模块：

- 用户登录
- JWT 鉴权
- 当前登录用户信息获取

## 1. 基础信息

- 服务默认地址：`http://localhost:8080`
- 数据返回格式：`application/json`
- 鉴权方式：`JWT Bearer Token`

## 2. 鉴权说明

除登录接口外，其余接口都需要在请求头中携带 token：

```http
Authorization: Bearer <accessToken>
```

`accessToken` 通过登录接口获取。

如果未携带 token，或 token 无效、已过期，后端返回：

```json
{
  "code": 401,
  "message": "未认证或 token 无效"
}
```

## 3. 接口列表

### 3.1 用户登录

- 请求方式：`POST`
- 请求路径：`/api/auth/login`
- 是否需要鉴权：否

#### 请求体

```json
{
  "accountNo": "admin001",
  "password": "123123",
  "role": "admin"
}
```

#### 请求参数说明

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `accountNo` | `string` | 是 | 登录账号，学生可传学号，教师可传工号，管理员可传管理员账号 |
| `password` | `string` | 是 | 登录密码 |
| `role` | `string` | 是 | 用户角色，只允许：`admin`、`teacher`、`student` |

#### 成功响应示例

```json
{
  "accessToken": "<JWT_TOKEN>",
  "expiresIn": 7200,
  "user": {
    "id": 1,
    "accountNo": "admin001",
    "role": "admin",
    "realName": "系统管理员",
    "phone": "13800000001",
    "email": "admin@scnu.example",
    "status": "active"
  }
}
```

#### 成功响应字段说明

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `accessToken` | `string` | JWT 访问令牌 |
| `expiresIn` | `number` | token 有效期，单位秒 |
| `user.id` | `number` | 用户主键 |
| `user.accountNo` | `string` | 登录账号 |
| `user.role` | `string` | 用户角色 |
| `user.realName` | `string` | 用户姓名 |
| `user.phone` | `string \| null` | 手机号 |
| `user.email` | `string \| null` | 邮箱 |
| `user.status` | `string` | 用户状态 |

#### 失败响应示例

账号或密码错误：

```json
{
  "code": 401,
  "message": "账号或密码错误"
}
```

参数校验失败：

```json
{
  "code": 400,
  "message": "角色必须是 admin、teacher 或 student"
}
```

#### 业务规则

- 仅允许 `sys_users.status = 'active'` 且 `deleted_at IS NULL` 的用户登录
- 登录成功后会更新：
  - `last_login_at`
  - `login_count`
- 当前代码兼容两种密码格式：
  - 明文占位密码
  - BCrypt 哈希密码

### 3.2 获取当前登录用户信息

- 请求方式：`GET`
- 请求路径：`/api/auth/me`
- 是否需要鉴权：是

#### 请求头

```http
Authorization: Bearer <JWT_TOKEN>
```

#### 成功响应示例

```json
{
  "id": 1,
  "accountNo": "admin001",
  "role": "admin",
  "realName": "系统管理员",
  "phone": "13800000001",
  "email": "admin@scnu.example",
  "status": "active"
}
```

#### 响应字段说明

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `number` | 用户主键 |
| `accountNo` | `string` | 登录账号 |
| `role` | `string` | 用户角色 |
| `realName` | `string` | 用户姓名 |
| `phone` | `string \| null` | 手机号 |
| `email` | `string \| null` | 邮箱 |
| `status` | `string` | 用户状态 |

#### 失败响应示例

未携带 token 或 token 无效：

```json
{
  "code": 401,
  "message": "未认证或 token 无效"
}
```

## 4. Apifox 调试建议

### 4.1 登录接口

- Method：`POST`
- URL：`http://localhost:8080/api/auth/login`
- Body 类型：`JSON`

请求体示例：

```json
{
  "accountNo": "admin001",
  "password": "123123",
  "role": "admin"
}
```

### 4.2 鉴权接口

先调用登录接口拿到 `accessToken`，再请求：

- Method：`GET`
- URL：`http://localhost:8080/api/auth/me`
- Header：

```http
Authorization: Bearer <accessToken>
```

## 5. 当前已实现范围

当前仅实现以下能力：

- 登录校验
- JWT 生成
- JWT 解析
- Spring Security 鉴权拦截
- 当前用户信息读取

当前未实现：

- 注册
- 退出登录
- 刷新 token
- 修改密码
- 用户管理
- 实验室管理
- 预约申请与审批
