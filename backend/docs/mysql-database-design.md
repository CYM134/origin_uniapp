# 实验室空间预约与协同管理系统 MySQL 数据库表结构设计文档

## 1. 设计依据

本文档根据当前 `origin_uniapp` 前端工程反推数据库结构。项目目前没有后端接口、ORM 模型或 SQL 迁移文件，业务数据主要通过 `uni.getStorageSync` / `uni.setStorageSync` 存在本地。

主要字段来源：

- 登录/注册：`student-login`、`student-register`、`teacher-login`、`teacher-register`、`admin-login`
- 个人信息：`student-personal-info`、`teacher-personal-info`
- 实验室管理：`admin-lab-management`
- 课表导入/预览：`admin-schedule-management`、`student-schedule-preview`、`teacher-schedule-preview`
- 预约申请：`student-reservation-apply`、`teacher-reservation-apply`
- 预约流程：`student-pending-process`、`student-completed-process`、`teacher-pending-process`、`teacher-completed-process`
- 管理员审核/记录：`admin-reservation-approval`、`admin-approval-records`
- 系统配置/用户/备份：`admin-system-management`

## 2. 数据库约定

- 数据库引擎：`InnoDB`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_0900_ai_ci`，如 MySQL 5.7 使用 `utf8mb4_unicode_ci`
- 主键：统一使用 `BIGINT UNSIGNED AUTO_INCREMENT`
- 时间字段：统一使用 `DATETIME(3)`，预约日期使用 `DATE`，预约时刻使用 `TIME`
- 软删除：重要业务表使用 `deleted_at`
- 密码：只保存 `password_hash`，禁止保存明文密码
- 前端的 `rememberPassword`、`showPassword`、`pickerIndex`、`filteredList`、`statusText`、`progressText` 等 UI 状态不应落库

## 3. 核心业务状态

### 用户状态

| 状态 | 说明 |
| --- | --- |
| `active` | 正常可登录 |
| `disabled` | 被管理员禁用，不能登录 |
| `deleted` | 逻辑删除 |
| `pending` | 待审核，例如教师注册申请未通过时 |

### 预约状态

| 状态 | 说明 |
| --- | --- |
| `draft` | 草稿，对应学生/教师申请页保存草稿 |
| `pending` | 已提交，待教师审核；教师自己提交的预约也可进入管理员待审 |
| `teacher_approved` | 教师已通过，待管理员最终审核 |
| `approved` | 审核通过，可使用实验室 |
| `rejected` | 被教师或管理员驳回 |
| `cancelled` | 申请人取消 |
| `completed` | 使用完成，或预约日期已过期后自动完成 |

### 审批阶段

| 阶段 | 说明 |
| --- | --- |
| `teacher` | 教师审核学生申请 |
| `admin` | 管理员终审预约 |
| `system` | 系统自动变更，如过期自动完成 |

## 4. 表关系概览

- `sys_users` 统一保存管理员、教师、学生账号。
- `student_profiles` 与 `teacher_profiles` 分别保存学生和教师扩展信息。
- `teacher_registration_applications` 保存教师注册审核材料和审核结果。
- `laboratories` 保存实验室基础资料。
- `academic_semesters`、`course_schedules` 保存课表数据。
- `reservation_applications` 保存学生/教师预约申请主数据。
- `reservation_approval_logs` 保存教师审核、管理员审核、取消、完成、重新审核等流程流水。
- `reservation_attachments` 保存预约附件。
- `notifications` 保存学生/教师消息通知。
- `system_settings` 保存系统配置。
- `data_backup_records` 保存备份记录。
- `audit_logs` 保存登录和关键操作日志。

## 5. 表结构明细

### 5.1 `sys_users` 用户账号表

用于统一管理管理员、教师、学生登录账号，对应前端 `registeredStudents`、`registeredTeachers`、管理员用户管理列表。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 用户内部主键，供其他表外键引用 |
| `account_no` | `VARCHAR(64)` | NOT NULL | 登录账号；学生为学号，教师为工号，管理员为管理员账号 |
| `role` | `ENUM('admin','teacher','student')` | NOT NULL | 用户角色，用于权限判断和登录入口区分 |
| `password_hash` | `VARCHAR(255)` | NOT NULL | 加密后的登录密码，替代前端当前明文 `password` |
| `real_name` | `VARCHAR(50)` | NOT NULL | 用户真实姓名，对应学生/教师姓名和管理员名称 |
| `gender` | `ENUM('male','female','unknown')` | DEFAULT `unknown` | 性别，学生和教师个人信息页使用 |
| `phone` | `VARCHAR(20)` | NULL | 手机号，预约联系人默认值和个人资料展示使用 |
| `email` | `VARCHAR(120)` | NULL | 邮箱，教师注册审核页面已有邮箱字段 |
| `avatar_url` | `VARCHAR(500)` | NULL | 头像地址，对应个人信息页头像更新和用户管理头像 |
| `status` | `ENUM('active','disabled','deleted','pending')` | NOT NULL DEFAULT `active` | 账号状态，支持管理员启用/禁用/删除用户 |
| `last_login_at` | `DATETIME(3)` | NULL | 最近登录时间，对应用户管理的 `lastLogin` |
| `login_count` | `INT UNSIGNED` | NOT NULL DEFAULT `0` | 登录次数，便于后台审计和统计 |
| `agreed_terms_at` | `DATETIME(3)` | NULL | 用户同意协议时间，对应注册页勾选用户协议 |
| `created_at` | `DATETIME(3)` | NOT NULL | 账号创建时间，对应前端 `registerTime` |
| `updated_at` | `DATETIME(3)` | NOT NULL | 账号资料最近更新时间 |
| `deleted_at` | `DATETIME(3)` | NULL | 软删除时间 |

建议索引：

- `UNIQUE KEY uk_role_account (role, account_no)`
- `KEY idx_role_status (role, status)`
- `KEY idx_phone (phone)`

### 5.2 `student_profiles` 学生资料表

用于保存学生特有信息，对应学生注册和学生个人信息页。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 学生资料主键 |
| `user_id` | `BIGINT UNSIGNED` | NOT NULL, UNIQUE | 关联 `sys_users.id` |
| `student_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 学号，对应前端 `studentId` |
| `college` | `VARCHAR(100)` | NOT NULL | 所属学院，对应注册和个人信息页的学院 |
| `major` | `VARCHAR(100)` | NOT NULL | 年级专业/专业信息，对应前端 `major` |
| `created_at` | `DATETIME(3)` | NOT NULL | 资料创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 资料更新时间 |

建议索引：

- `UNIQUE KEY uk_student_no (student_no)`
- `UNIQUE KEY uk_user_id (user_id)`

### 5.3 `teacher_profiles` 教师资料表

用于保存教师特有信息，对应教师注册、教师个人信息、教师预约申请。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 教师资料主键 |
| `user_id` | `BIGINT UNSIGNED` | NOT NULL, UNIQUE | 关联 `sys_users.id` |
| `teacher_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 工号，对应前端 `teacherId` |
| `college` | `VARCHAR(100)` | NOT NULL | 所属学院 |
| `department` | `VARCHAR(100)` | NULL | 部门，对应教师个人信息页 `department` |
| `position_title` | `VARCHAR(50)` | NULL | 职位/职称，对应教师注册审核页的教授、副教授、讲师等 |
| `created_at` | `DATETIME(3)` | NOT NULL | 资料创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 资料更新时间 |

建议索引：

- `UNIQUE KEY uk_teacher_no (teacher_no)`
- `UNIQUE KEY uk_user_id (user_id)`

### 5.4 `teacher_registration_applications` 教师注册审核表

用于管理员审核教师注册申请，对应 `admin-teacher-registration` 页。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 教师注册申请主键 |
| `application_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 申请编号，例如 `T20230001` |
| `teacher_no` | `VARCHAR(64)` | NOT NULL | 教师工号 |
| `real_name` | `VARCHAR(50)` | NOT NULL | 教师姓名 |
| `gender` | `ENUM('male','female','unknown')` | DEFAULT `unknown` | 性别 |
| `college` | `VARCHAR(100)` | NULL | 所属学院 |
| `department` | `VARCHAR(100)` | NULL | 院系/部门 |
| `position_title` | `VARCHAR(50)` | NULL | 职位或职称 |
| `phone` | `VARCHAR(20)` | NOT NULL | 联系电话 |
| `email` | `VARCHAR(120)` | NULL | 邮箱 |
| `password_hash` | `VARCHAR(255)` | NOT NULL | 注册时设置的密码哈希；审核通过后写入 `sys_users` |
| `id_card_front_url` | `VARCHAR(500)` | NULL | 身份证正面或证件材料图片地址 |
| `id_card_back_url` | `VARCHAR(500)` | NULL | 身份证反面或证件材料图片地址 |
| `teacher_card_image_url` | `VARCHAR(500)` | NULL | 教师证/教师证明图片地址 |
| `status` | `ENUM('pending','approved','rejected')` | NOT NULL DEFAULT `pending` | 注册审核状态 |
| `reject_reason` | `VARCHAR(500)` | NULL | 拒绝原因 |
| `reviewer_user_id` | `BIGINT UNSIGNED` | NULL | 审核管理员，关联 `sys_users.id` |
| `applied_at` | `DATETIME(3)` | NOT NULL | 申请提交时间，对应 `registerTime` |
| `reviewed_at` | `DATETIME(3)` | NULL | 审核时间，对应 `approvalTime` |
| `created_at` | `DATETIME(3)` | NOT NULL | 记录创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 记录更新时间 |

建议索引：

- `KEY idx_status_applied (status, applied_at)`
- `KEY idx_teacher_no (teacher_no)`

### 5.5 `laboratories` 实验室表

用于保存实验室基础信息，对应 `admin-lab-management`、预约页和课表页的实验室选择。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 实验室主键 |
| `lab_code` | `VARCHAR(64)` | NOT NULL, UNIQUE | 实验室业务编码，对应前端 `lab1`、`lab001`、`1` 等 |
| `name` | `VARCHAR(100)` | NOT NULL | 实验室名称 |
| `location` | `VARCHAR(200)` | NOT NULL | 位置，例如综合楼东 A301 |
| `capacity` | `INT UNSIGNED` | NOT NULL DEFAULT `0` | 最大可容纳人数，对应 `capacity` / `maxStudents` |
| `equipment` | `TEXT` | NULL | 设备情况说明 |
| `image_url` | `VARCHAR(500)` | NULL | 实验室预览图片 |
| `status` | `ENUM('active','maintenance','disabled','deleted')` | NOT NULL DEFAULT `active` | 实验室状态；维修或停用时不能预约 |
| `created_by` | `BIGINT UNSIGNED` | NULL | 创建管理员，关联 `sys_users.id` |
| `updated_by` | `BIGINT UNSIGNED` | NULL | 最近修改管理员 |
| `created_at` | `DATETIME(3)` | NOT NULL | 创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 更新时间 |
| `deleted_at` | `DATETIME(3)` | NULL | 删除时间 |

建议索引：

- `UNIQUE KEY uk_lab_code (lab_code)`
- `KEY idx_status (status)`
- `KEY idx_name (name)`

### 5.6 `academic_semesters` 学期表

用于课表导入、导出按学期管理，对应 `admin-schedule-management` 中的学期选项。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 学期主键 |
| `semester_code` | `VARCHAR(64)` | NOT NULL, UNIQUE | 学期编码，例如 `2025-2026-1` |
| `semester_name` | `VARCHAR(100)` | NOT NULL | 学期名称，例如 `2025-2026学年第一学期` |
| `academic_year` | `VARCHAR(20)` | NOT NULL | 学年，例如 `2025-2026` |
| `term_no` | `TINYINT UNSIGNED` | NOT NULL | 第几学期，通常 `1` 或 `2` |
| `start_date` | `DATE` | NULL | 学期开始日期 |
| `end_date` | `DATE` | NULL | 学期结束日期 |
| `status` | `ENUM('active','inactive')` | NOT NULL DEFAULT `inactive` | 是否当前启用学期 |
| `created_at` | `DATETIME(3)` | NOT NULL | 创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 更新时间 |

建议索引：

- `UNIQUE KEY uk_semester_code (semester_code)`
- `KEY idx_status (status)`

### 5.7 `schedule_import_batches` 课表导入批次表

用于记录管理员导入课表文件的过程和结果，对应 `admin-schedule-management` 的导入进度、错误详情。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 导入批次主键 |
| `batch_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 导入批次号 |
| `semester_id` | `BIGINT UNSIGNED` | NOT NULL | 关联 `academic_semesters.id` |
| `file_name` | `VARCHAR(255)` | NOT NULL | 上传文件名 |
| `file_size_bytes` | `BIGINT UNSIGNED` | NULL | 文件大小，单位字节 |
| `file_path` | `VARCHAR(500)` | NULL | 文件保存路径 |
| `status` | `ENUM('pending','parsing','validating','saving','success','failed')` | NOT NULL DEFAULT `pending` | 导入状态 |
| `progress` | `TINYINT UNSIGNED` | NOT NULL DEFAULT `0` | 导入进度百分比 |
| `success_count` | `INT UNSIGNED` | NOT NULL DEFAULT `0` | 成功导入的课表记录数 |
| `failure_count` | `INT UNSIGNED` | NOT NULL DEFAULT `0` | 导入失败的记录数 |
| `error_details` | `TEXT` | NULL | 导入失败详情，对应页面错误提示 |
| `imported_by` | `BIGINT UNSIGNED` | NOT NULL | 操作管理员，关联 `sys_users.id` |
| `started_at` | `DATETIME(3)` | NULL | 导入开始时间 |
| `finished_at` | `DATETIME(3)` | NULL | 导入结束时间 |
| `created_at` | `DATETIME(3)` | NOT NULL | 记录创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 记录更新时间 |

建议索引：

- `KEY idx_semester_status (semester_id, status)`
- `KEY idx_created_at (created_at)`

### 5.8 `schedule_export_tasks` 课表导出任务表

用于记录管理员导出课表的格式和包含项，对应课表导出页面。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 导出任务主键 |
| `task_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 导出任务编号 |
| `semester_id` | `BIGINT UNSIGNED` | NOT NULL | 导出的学期 |
| `export_format` | `ENUM('excel','pdf')` | NOT NULL | 导出格式 |
| `include_rooms` | `TINYINT(1)` | NOT NULL DEFAULT `1` | 是否包含教室/实验室信息 |
| `include_teachers` | `TINYINT(1)` | NOT NULL DEFAULT `1` | 是否包含教师信息 |
| `include_students` | `TINYINT(1)` | NOT NULL DEFAULT `0` | 是否包含学生名单 |
| `file_url` | `VARCHAR(500)` | NULL | 生成后的文件下载地址 |
| `status` | `ENUM('pending','processing','success','failed')` | NOT NULL DEFAULT `pending` | 导出任务状态 |
| `error_message` | `TEXT` | NULL | 导出失败原因 |
| `created_by` | `BIGINT UNSIGNED` | NOT NULL | 操作管理员 |
| `created_at` | `DATETIME(3)` | NOT NULL | 创建时间 |
| `finished_at` | `DATETIME(3)` | NULL | 完成时间 |

建议索引：

- `KEY idx_semester_created (semester_id, created_at)`
- `KEY idx_created_by (created_by)`

### 5.9 `course_schedules` 课表表

用于保存导入或维护后的实验室课程安排，对应学生/教师课表预览页。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 课表记录主键 |
| `schedule_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 课表记录编号 |
| `semester_id` | `BIGINT UNSIGNED` | NOT NULL | 所属学期 |
| `lab_id` | `BIGINT UNSIGNED` | NOT NULL | 实验室，关联 `laboratories.id` |
| `teacher_user_id` | `BIGINT UNSIGNED` | NULL | 授课教师，关联 `sys_users.id` |
| `teacher_name_snapshot` | `VARCHAR(50)` | NULL | 教师姓名快照，导入时没有用户账号也可显示 |
| `course_name` | `VARCHAR(150)` | NOT NULL | 课程名称，对应 `courseName` / `name` |
| `course_type` | `VARCHAR(50)` | NULL | 课程类型，例如理论课、实验课、考试、其他活动 |
| `schedule_date` | `DATE` | NOT NULL | 上课日期 |
| `weekday` | `TINYINT UNSIGNED` | NULL | 周几，1-7 |
| `start_time` | `TIME` | NOT NULL | 开始时间 |
| `end_time` | `TIME` | NOT NULL | 结束时间 |
| `time_slot_label` | `VARCHAR(50)` | NULL | 展示用时间段，例如 `08:00-09:50` |
| `planned_student_count` | `INT UNSIGNED` | NOT NULL DEFAULT `0` | 计划人数，对应教师课表 `studentCount` |
| `current_student_count` | `INT UNSIGNED` | NOT NULL DEFAULT `0` | 当前人数，对应学生课表 `currentStudents` |
| `max_student_count` | `INT UNSIGNED` | NOT NULL DEFAULT `0` | 最大人数，对应学生课表 `maxStudents` |
| `status` | `ENUM('scheduled','available','full','ongoing','cancelled')` | NOT NULL DEFAULT `scheduled` | 课表状态，支持可查看、已满、进行中等展示 |
| `can_reserve` | `TINYINT(1)` | NOT NULL DEFAULT `0` | 是否允许基于该课表发起预约 |
| `description` | `TEXT` | NULL | 课程说明，对应课表详情描述 |
| `remark` | `TEXT` | NULL | 备注 |
| `source_batch_id` | `BIGINT UNSIGNED` | NULL | 来源导入批次，关联 `schedule_import_batches.id` |
| `created_at` | `DATETIME(3)` | NOT NULL | 创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 更新时间 |
| `deleted_at` | `DATETIME(3)` | NULL | 删除时间 |

建议索引：

- `KEY idx_date_lab (schedule_date, lab_id)`
- `KEY idx_teacher_date (teacher_user_id, schedule_date)`
- `KEY idx_semester_date (semester_id, schedule_date)`

### 5.10 `reservation_applications` 预约申请主表

用于统一保存学生预约和教师预约。学生预约字段与教师预约字段不完全一致，因此部分字段允许为空。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 预约申请主键 |
| `application_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 预约编号，对应前端 `app_时间戳` 或管理员展示的 `R20230001` |
| `applicant_user_id` | `BIGINT UNSIGNED` | NOT NULL | 申请人，关联 `sys_users.id` |
| `applicant_role` | `ENUM('student','teacher')` | NOT NULL | 申请人类型，对应学生预约或教师预约 |
| `applicant_no` | `VARCHAR(64)` | NOT NULL | 申请人学号/工号快照 |
| `applicant_name` | `VARCHAR(50)` | NOT NULL | 申请人姓名快照，对应 `applicant`、`studentName`、`teacherName` |
| `applicant_phone` | `VARCHAR(20)` | NULL | 联系电话快照，对应 `contact`、`teacherPhone`、`applicantPhone` |
| `lab_id` | `BIGINT UNSIGNED` | NOT NULL | 预约实验室，关联 `laboratories.id` |
| `lab_name_snapshot` | `VARCHAR(100)` | NOT NULL | 实验室名称快照，避免实验室改名后历史记录展示变化 |
| `schedule_id` | `BIGINT UNSIGNED` | NULL | 关联课表记录；普通自主预约可为空 |
| `reserve_date` | `DATE` | NOT NULL | 预约日期，对应 `date` |
| `start_time` | `TIME` | NOT NULL | 预约开始时间 |
| `end_time` | `TIME` | NOT NULL | 预约结束时间 |
| `time_slot_label` | `VARCHAR(50)` | NULL | 展示用时间段，对应 `timeSlot` |
| `participant_count` | `INT UNSIGNED` | NOT NULL DEFAULT `1` | 使用人数，对应 `studentCount` |
| `application_type` | `VARCHAR(50)` | NULL | 学生申请类型编码，例如 `course`、`research`、`competition` |
| `application_type_name` | `VARCHAR(50)` | NULL | 学生申请类型名称，例如课程实验、科研项目 |
| `title` | `VARCHAR(150)` | NULL | 学生申请主题，对应 `title` |
| `purpose` | `TEXT` | NULL | 使用目的，对应学生申请 `purpose` |
| `instructor_name` | `VARCHAR(50)` | NULL | 指导教师/负责教师文本，对应学生申请 `teacher` |
| `requirements` | `TEXT` | NULL | 特殊需求，对应学生申请 `requirements` |
| `emergency_contact_name` | `VARCHAR(50)` | NULL | 应急联系人，对应 `emergencyContact` |
| `emergency_contact_phone` | `VARCHAR(20)` | NULL | 应急联系人电话，对应 `emergencyPhone` |
| `course_name` | `VARCHAR(150)` | NULL | 教师预约课程名称，对应 `courseName` |
| `course_type` | `VARCHAR(50)` | NULL | 教师预约课程类型，对应 `courseType` |
| `remark` | `TEXT` | NULL | 教师预约备注，或学生申请在教师端展示的备注 |
| `status` | `ENUM('draft','pending','teacher_approved','approved','rejected','cancelled','completed')` | NOT NULL DEFAULT `draft` | 当前流程状态 |
| `is_completed` | `TINYINT(1)` | NOT NULL DEFAULT `0` | 是否已完成，对应前端 `isCompleted` |
| `submitted_at` | `DATETIME(3)` | NULL | 提交时间，对应 `submitTime`；草稿为空 |
| `teacher_review_user_id` | `BIGINT UNSIGNED` | NULL | 教师审核人 |
| `teacher_review_name` | `VARCHAR(50)` | NULL | 教师审核人姓名快照，对应 `teacherReviewerName` |
| `teacher_review_at` | `DATETIME(3)` | NULL | 教师审核时间，对应 `teacherReviewTime` |
| `teacher_review_comment` | `VARCHAR(500)` | NULL | 教师审核意见，对应 `teacherReviewReason` |
| `admin_review_user_id` | `BIGINT UNSIGNED` | NULL | 管理员终审人 |
| `admin_review_name` | `VARCHAR(50)` | NULL | 管理员姓名快照 |
| `admin_review_at` | `DATETIME(3)` | NULL | 管理员终审时间，对应管理员审核页 `approvalTime` |
| `admin_review_comment` | `VARCHAR(500)` | NULL | 管理员审核意见 |
| `reject_reason` | `VARCHAR(500)` | NULL | 最近一次驳回原因，列表快速展示使用 |
| `cancelled_at` | `DATETIME(3)` | NULL | 取消时间，对应 `cancelTime` |
| `completed_at` | `DATETIME(3)` | NULL | 完成时间，对应 `completedTime` |
| `source_application_id` | `BIGINT UNSIGNED` | NULL | 重新申请或修改申请时关联原申请 |
| `created_at` | `DATETIME(3)` | NOT NULL | 创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 更新时间 |
| `deleted_at` | `DATETIME(3)` | NULL | 软删除时间 |

建议索引：

- `UNIQUE KEY uk_application_no (application_no)`
- `KEY idx_applicant_status (applicant_user_id, status)`
- `KEY idx_lab_date_time (lab_id, reserve_date, start_time, end_time)`
- `KEY idx_status_submitted (status, submitted_at)`
- `KEY idx_teacher_review (teacher_review_user_id, teacher_review_at)`

并发约束建议：

- 同一实验室同一日期的时间段不能重叠。MySQL 不能用普通唯一索引直接限制时间区间重叠，后端提交时应在事务中查询 `lab_id + reserve_date` 的已通过/待审记录，并使用行锁或业务锁防止并发冲突。

### 5.11 `reservation_approval_logs` 预约审批流水表

用于保存完整流程历史。即使主表只保留当前状态，该表也能追踪谁在什么时候做了什么操作。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 审批流水主键 |
| `application_id` | `BIGINT UNSIGNED` | NOT NULL | 关联 `reservation_applications.id` |
| `stage` | `ENUM('teacher','admin','system')` | NOT NULL | 审批阶段：教师、管理员、系统 |
| `action` | `ENUM('submit','approve','reject','cancel','complete','reprocess')` | NOT NULL | 操作类型 |
| `from_status` | `VARCHAR(32)` | NULL | 操作前状态 |
| `to_status` | `VARCHAR(32)` | NOT NULL | 操作后状态 |
| `reviewer_user_id` | `BIGINT UNSIGNED` | NULL | 操作人；系统自动操作可为空 |
| `reviewer_name_snapshot` | `VARCHAR(50)` | NULL | 操作人姓名快照 |
| `comment` | `VARCHAR(500)` | NULL | 审核意见、驳回原因、重新审核说明 |
| `created_at` | `DATETIME(3)` | NOT NULL | 操作发生时间 |

建议索引：

- `KEY idx_application_created (application_id, created_at)`
- `KEY idx_reviewer_created (reviewer_user_id, created_at)`

### 5.12 `reservation_attachments` 预约附件表

用于保存预约相关附件，对应管理员预约详情中的 `attachments`。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 附件主键 |
| `application_id` | `BIGINT UNSIGNED` | NOT NULL | 关联 `reservation_applications.id` |
| `file_name` | `VARCHAR(255)` | NOT NULL | 原始文件名，例如实验计划文档 |
| `file_url` | `VARCHAR(500)` | NOT NULL | 文件访问地址 |
| `file_type` | `VARCHAR(80)` | NULL | 文件 MIME 类型或扩展名 |
| `file_size_bytes` | `BIGINT UNSIGNED` | NULL | 文件大小，单位字节 |
| `uploaded_by` | `BIGINT UNSIGNED` | NULL | 上传用户 |
| `created_at` | `DATETIME(3)` | NOT NULL | 上传时间 |

建议索引：

- `KEY idx_application (application_id)`

### 5.13 `notifications` 通知表

用于保存学生和教师通知，对应前端 `notifications`、`teacherNotifications`。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 通知主键 |
| `notification_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 通知编号，对应前端 `notif_时间戳` |
| `recipient_user_id` | `BIGINT UNSIGNED` | NOT NULL | 接收人，关联 `sys_users.id` |
| `recipient_role` | `ENUM('student','teacher','admin')` | NOT NULL | 接收人角色，便于按端查询 |
| `recipient_account_no` | `VARCHAR(64)` | NOT NULL | 接收人账号快照，例如学生学号 |
| `title` | `VARCHAR(150)` | NOT NULL | 通知标题 |
| `content` | `TEXT` | NOT NULL | 通知正文 |
| `type` | `ENUM('info','success','warning','error')` | NOT NULL DEFAULT `info` | 通知类型，对应前端 success/warning 图标样式 |
| `related_application_id` | `BIGINT UNSIGNED` | NULL | 关联预约申请 |
| `is_read` | `TINYINT(1)` | NOT NULL DEFAULT `0` | 是否已读，对应前端 `read` |
| `read_at` | `DATETIME(3)` | NULL | 阅读时间 |
| `created_at` | `DATETIME(3)` | NOT NULL | 创建时间，对应 `createTime` |

建议索引：

- `KEY idx_recipient_read (recipient_user_id, is_read, created_at)`
- `KEY idx_application (related_application_id)`

### 5.14 `system_settings` 系统设置表

用于保存管理员系统设置页配置。该表通常只保留一条当前配置记录。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 设置记录主键 |
| `system_name` | `VARCHAR(100)` | NOT NULL | 系统名称 |
| `school_name` | `VARCHAR(100)` | NOT NULL | 学校名称 |
| `logo_url` | `VARCHAR(500)` | NULL | 系统 Logo 地址 |
| `reservation_start_time` | `TIME` | NOT NULL | 每日允许预约开始时间 |
| `reservation_end_time` | `TIME` | NOT NULL | 每日允许预约结束时间 |
| `advance_days` | `INT UNSIGNED` | NOT NULL DEFAULT `3` | 允许提前预约天数 |
| `auto_approval` | `TINYINT(1)` | NOT NULL DEFAULT `0` | 是否自动审批 |
| `reservation_notification_enabled` | `TINYINT(1)` | NOT NULL DEFAULT `1` | 是否开启预约通知 |
| `approval_notification_enabled` | `TINYINT(1)` | NOT NULL DEFAULT `1` | 是否开启审核结果通知 |
| `reminder_notification_enabled` | `TINYINT(1)` | NOT NULL DEFAULT `1` | 是否开启预约提醒 |
| `updated_by` | `BIGINT UNSIGNED` | NULL | 最近修改管理员 |
| `created_at` | `DATETIME(3)` | NOT NULL | 创建时间 |
| `updated_at` | `DATETIME(3)` | NOT NULL | 更新时间 |

建议约束：

- 后端保证只存在一条有效配置，或增加 `config_key` 支持多套配置。

### 5.15 `data_backup_records` 数据备份记录表

用于保存系统备份记录，对应管理员系统管理页的备份列表。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 备份记录主键 |
| `backup_no` | `VARCHAR(64)` | NOT NULL, UNIQUE | 备份编号，对应前端 `backup001` |
| `backup_type` | `ENUM('labs','schedules','reservations','users','all')` | NOT NULL | 备份类型 |
| `backup_name` | `VARCHAR(150)` | NOT NULL | 备份名称 |
| `file_url` | `VARCHAR(500)` | NULL | 备份文件下载地址 |
| `file_size_bytes` | `BIGINT UNSIGNED` | NULL | 备份文件大小，单位字节 |
| `display_size` | `VARCHAR(50)` | NULL | 展示用大小，例如 `5.2MB` |
| `status` | `ENUM('processing','success','failed','deleted')` | NOT NULL DEFAULT `processing` | 备份状态 |
| `progress` | `TINYINT UNSIGNED` | NOT NULL DEFAULT `0` | 备份进度百分比 |
| `created_by` | `BIGINT UNSIGNED` | NOT NULL | 操作管理员 |
| `started_at` | `DATETIME(3)` | NOT NULL | 开始备份时间 |
| `completed_at` | `DATETIME(3)` | NULL | 备份完成时间 |
| `restored_at` | `DATETIME(3)` | NULL | 最近恢复时间 |
| `deleted_at` | `DATETIME(3)` | NULL | 删除时间 |

建议索引：

- `KEY idx_type_status (backup_type, status)`
- `KEY idx_started_at (started_at)`

### 5.16 `audit_logs` 操作日志表

用于保存登录、导入、审核、删除实验室、重置密码等关键操作。前端 `logs` 页目前只保存时间戳，后端应记录更完整审计信息。

| 字段名 | 类型 | 约束 | 用途 |
| --- | --- | --- | --- |
| `id` | `BIGINT UNSIGNED` | PK, AUTO_INCREMENT | 日志主键 |
| `user_id` | `BIGINT UNSIGNED` | NULL | 操作用户；未登录行为可为空 |
| `role` | `ENUM('admin','teacher','student','anonymous')` | NOT NULL | 操作时角色 |
| `module` | `VARCHAR(50)` | NOT NULL | 模块名称，例如 `reservation`、`lab`、`schedule`、`user` |
| `action` | `VARCHAR(50)` | NOT NULL | 操作名称，例如 `login`、`approve`、`delete_lab` |
| `target_type` | `VARCHAR(50)` | NULL | 操作对象类型，例如 `reservation_application` |
| `target_id` | `BIGINT UNSIGNED` | NULL | 操作对象 ID |
| `detail` | `JSON` | NULL | 操作详情，保存变更前后摘要、失败原因等 |
| `ip_address` | `VARCHAR(64)` | NULL | 客户端 IP |
| `user_agent` | `VARCHAR(500)` | NULL | 客户端 UA |
| `created_at` | `DATETIME(3)` | NOT NULL | 日志时间 |

建议索引：

- `KEY idx_user_created (user_id, created_at)`
- `KEY idx_module_action_created (module, action, created_at)`
- `KEY idx_target (target_type, target_id)`

## 6. 不建议落库的前端字段

以下字段是 UI 状态或可由后端计算，不建议作为业务字段直接保存：

| 前端字段 | 原因 |
| --- | --- |
| `rememberPassword`、`studentPassword`、`teacherPassword`、`adminPassword` | 不能在数据库或本地保存明文密码，应使用 token/session 和密码哈希 |
| `showPassword`、`showModal`、`showDetailModal`、`pickerIndex` | 页面交互状态，不属于业务数据 |
| `filteredApplications`、`filteredUsers`、`filteredLabs` | 查询结果，后端通过 SQL 条件查询即可 |
| `statusText`、`statusClass`、`progressText` | 展示文本和样式，可由 `status` 映射生成 |
| `submitTimeText`、`reviewTimeText` | 格式化展示字段，可由时间字段格式化得到 |
| `pendingCount`、`notificationCount`、`stats` | 统计结果，应通过查询实时计算或缓存 |

## 7. 初始化数据建议

系统初始化时至少写入：

- 一个管理员账号：`sys_users.role = 'admin'`
- 当前系统配置：`system_settings`
- 初始实验室：国际课程实验室、IBC 实验中心、互联网+新商科实验室等
- 学期字典：`academic_semesters`

## 8. 后端实现注意点

- 注册、登录、修改密码必须使用密码哈希，推荐 `bcrypt`、`argon2` 或等效方案。
- 预约提交必须做时间冲突校验，重点校验 `lab_id + reserve_date + start_time/end_time`。
- 教师审核学生申请时，把主表状态从 `pending` 更新为 `teacher_approved` 或 `rejected`，同时写入 `reservation_approval_logs`。
- 管理员终审时，把状态更新为 `approved` 或 `rejected`，同时写入审批流水和通知。
- 学生取消时只允许取消自己的未完成申请，更新 `cancelled_at` 并写流水。
- 自动完成可以由定时任务扫描 `approved` 且预约日期已过的记录，更新为 `completed`。
- 附件、头像、备份文件只在数据库保存 URL 或对象存储 key，不建议把文件二进制直接存入 MySQL。
