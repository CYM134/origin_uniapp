-- Lab reservation management system schema.
-- Target: MySQL 8.0+, InnoDB, utf8mb4.
-- This file is intentionally non-destructive: it does not DROP existing tables.

SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE DATABASE IF NOT EXISTS lab_reservation
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE lab_reservation;

CREATE TABLE IF NOT EXISTS sys_users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Internal user primary key',
  account_no VARCHAR(64) NOT NULL COMMENT 'Login account: student number, teacher number, or admin account',
  role ENUM('admin','teacher','student') NOT NULL COMMENT 'User role',
  password_hash VARCHAR(255) NOT NULL COMMENT 'Hashed password, never store plaintext password',
  real_name VARCHAR(50) NOT NULL COMMENT 'Real display name',
  gender ENUM('male','female','unknown') NOT NULL DEFAULT 'unknown' COMMENT 'Gender',
  phone VARCHAR(20) DEFAULT NULL COMMENT 'Mobile phone number',
  email VARCHAR(120) DEFAULT NULL COMMENT 'Email address',
  avatar_url VARCHAR(500) DEFAULT NULL COMMENT 'Avatar image URL',
  status ENUM('active','disabled','deleted','pending') NOT NULL DEFAULT 'active' COMMENT 'Account status',
  last_login_at DATETIME(3) DEFAULT NULL COMMENT 'Last login time',
  login_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Total successful login count',
  agreed_terms_at DATETIME(3) DEFAULT NULL COMMENT 'Time when user accepted terms',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  deleted_at DATETIME(3) DEFAULT NULL COMMENT 'Soft delete time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_account (role, account_no),
  KEY idx_role_status (role, status),
  KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Unified user account table';

CREATE TABLE IF NOT EXISTS student_profiles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Student profile primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked sys_users.id',
  student_no VARCHAR(64) NOT NULL COMMENT 'Student number',
  college VARCHAR(100) NOT NULL COMMENT 'College name',
  major VARCHAR(100) NOT NULL COMMENT 'Grade and major text',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id),
  UNIQUE KEY uk_student_no (student_no),
  CONSTRAINT fk_student_profiles_user
    FOREIGN KEY (user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Student extended profile';

CREATE TABLE IF NOT EXISTS teacher_profiles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Teacher profile primary key',
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked sys_users.id',
  teacher_no VARCHAR(64) NOT NULL COMMENT 'Teacher employee number',
  college VARCHAR(100) NOT NULL COMMENT 'College name',
  department VARCHAR(100) DEFAULT NULL COMMENT 'Department name',
  position_title VARCHAR(50) DEFAULT NULL COMMENT 'Position or academic title',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id),
  UNIQUE KEY uk_teacher_no (teacher_no),
  CONSTRAINT fk_teacher_profiles_user
    FOREIGN KEY (user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Teacher extended profile';

CREATE TABLE IF NOT EXISTS teacher_registration_applications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Teacher registration application primary key',
  application_no VARCHAR(64) NOT NULL COMMENT 'Business application number',
  teacher_no VARCHAR(64) NOT NULL COMMENT 'Teacher employee number',
  real_name VARCHAR(50) NOT NULL COMMENT 'Teacher real name',
  gender ENUM('male','female','unknown') NOT NULL DEFAULT 'unknown' COMMENT 'Gender',
  college VARCHAR(100) DEFAULT NULL COMMENT 'College name',
  department VARCHAR(100) DEFAULT NULL COMMENT 'Department name',
  position_title VARCHAR(50) DEFAULT NULL COMMENT 'Position or academic title',
  phone VARCHAR(20) NOT NULL COMMENT 'Contact phone',
  email VARCHAR(120) DEFAULT NULL COMMENT 'Email address',
  password_hash VARCHAR(255) NOT NULL COMMENT 'Hashed registration password',
  id_card_front_url VARCHAR(500) DEFAULT NULL COMMENT 'ID card or document front image URL',
  id_card_back_url VARCHAR(500) DEFAULT NULL COMMENT 'ID card or document back image URL',
  teacher_card_image_url VARCHAR(500) DEFAULT NULL COMMENT 'Teacher certificate image URL',
  status ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending' COMMENT 'Review status',
  reject_reason VARCHAR(500) DEFAULT NULL COMMENT 'Reject reason',
  reviewer_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Admin reviewer user id',
  applied_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Submitted time',
  reviewed_at DATETIME(3) DEFAULT NULL COMMENT 'Reviewed time',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_application_no (application_no),
  KEY idx_status_applied (status, applied_at),
  KEY idx_teacher_no (teacher_no),
  KEY idx_reviewer_user_id (reviewer_user_id),
  CONSTRAINT fk_teacher_registration_reviewer
    FOREIGN KEY (reviewer_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Teacher registration review applications';

CREATE TABLE IF NOT EXISTS laboratories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Laboratory primary key',
  lab_code VARCHAR(64) NOT NULL COMMENT 'Business laboratory code',
  name VARCHAR(100) NOT NULL COMMENT 'Laboratory name',
  location VARCHAR(200) NOT NULL COMMENT 'Laboratory location',
  capacity INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Maximum capacity',
  equipment TEXT COMMENT 'Equipment description',
  image_url VARCHAR(500) DEFAULT NULL COMMENT 'Preview image URL',
  status ENUM('active','maintenance','disabled','deleted') NOT NULL DEFAULT 'active' COMMENT 'Laboratory status',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT 'Creator admin user id',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT 'Last updater admin user id',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  deleted_at DATETIME(3) DEFAULT NULL COMMENT 'Soft delete time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_lab_code (lab_code),
  KEY idx_status (status),
  KEY idx_name (name),
  KEY idx_created_by (created_by),
  KEY idx_updated_by (updated_by),
  CONSTRAINT fk_laboratories_created_by
    FOREIGN KEY (created_by) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_laboratories_updated_by
    FOREIGN KEY (updated_by) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Laboratory basic information';

CREATE TABLE IF NOT EXISTS academic_semesters (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Semester primary key',
  semester_code VARCHAR(64) NOT NULL COMMENT 'Semester code, e.g. 2025-2026-1',
  semester_name VARCHAR(100) NOT NULL COMMENT 'Semester display name',
  academic_year VARCHAR(20) NOT NULL COMMENT 'Academic year, e.g. 2025-2026',
  term_no TINYINT UNSIGNED NOT NULL COMMENT 'Term number, usually 1 or 2',
  start_date DATE DEFAULT NULL COMMENT 'Semester start date',
  end_date DATE DEFAULT NULL COMMENT 'Semester end date',
  status ENUM('active','inactive') NOT NULL DEFAULT 'inactive' COMMENT 'Semester status',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_semester_code (semester_code),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Academic semesters';

CREATE TABLE IF NOT EXISTS schedule_import_batches (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Schedule import batch primary key',
  batch_no VARCHAR(64) NOT NULL COMMENT 'Import batch number',
  semester_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked academic_semesters.id',
  file_name VARCHAR(255) NOT NULL COMMENT 'Uploaded file name',
  file_size_bytes BIGINT UNSIGNED DEFAULT NULL COMMENT 'Uploaded file size in bytes',
  file_path VARCHAR(500) DEFAULT NULL COMMENT 'Stored file path',
  status ENUM('pending','parsing','validating','saving','success','failed') NOT NULL DEFAULT 'pending' COMMENT 'Import status',
  progress TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Import progress percentage',
  success_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Successful row count',
  failure_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Failed row count',
  error_details TEXT COMMENT 'Import error details',
  imported_by BIGINT UNSIGNED NOT NULL COMMENT 'Admin user who imports schedule',
  started_at DATETIME(3) DEFAULT NULL COMMENT 'Import start time',
  finished_at DATETIME(3) DEFAULT NULL COMMENT 'Import finish time',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_batch_no (batch_no),
  KEY idx_semester_status (semester_id, status),
  KEY idx_created_at (created_at),
  KEY idx_imported_by (imported_by),
  CONSTRAINT fk_schedule_import_semester
    FOREIGN KEY (semester_id) REFERENCES academic_semesters (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_schedule_import_imported_by
    FOREIGN KEY (imported_by) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_schedule_import_progress CHECK (progress <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Schedule import batch records';

CREATE TABLE IF NOT EXISTS schedule_export_tasks (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Schedule export task primary key',
  task_no VARCHAR(64) NOT NULL COMMENT 'Export task number',
  semester_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked academic_semesters.id',
  export_format ENUM('excel','pdf') NOT NULL COMMENT 'Export file format',
  include_rooms TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether exported data includes rooms',
  include_teachers TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether exported data includes teachers',
  include_students TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether exported data includes students',
  file_url VARCHAR(500) DEFAULT NULL COMMENT 'Generated file URL',
  status ENUM('pending','processing','success','failed') NOT NULL DEFAULT 'pending' COMMENT 'Export task status',
  error_message TEXT COMMENT 'Export failure message',
  created_by BIGINT UNSIGNED NOT NULL COMMENT 'Admin user who created export task',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  finished_at DATETIME(3) DEFAULT NULL COMMENT 'Task finish time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_no (task_no),
  KEY idx_semester_created (semester_id, created_at),
  KEY idx_created_by (created_by),
  CONSTRAINT fk_schedule_export_semester
    FOREIGN KEY (semester_id) REFERENCES academic_semesters (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_schedule_export_created_by
    FOREIGN KEY (created_by) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Schedule export task records';

CREATE TABLE IF NOT EXISTS course_schedules (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Course schedule primary key',
  schedule_no VARCHAR(64) NOT NULL COMMENT 'Business schedule number',
  semester_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked academic_semesters.id',
  lab_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked laboratories.id',
  teacher_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Linked teacher sys_users.id',
  teacher_name_snapshot VARCHAR(50) DEFAULT NULL COMMENT 'Teacher name snapshot',
  course_name VARCHAR(150) NOT NULL COMMENT 'Course name',
  course_type VARCHAR(50) DEFAULT NULL COMMENT 'Course type',
  schedule_date DATE NOT NULL COMMENT 'Course date',
  weekday TINYINT UNSIGNED DEFAULT NULL COMMENT 'Weekday from 1 to 7',
  start_time TIME NOT NULL COMMENT 'Start time',
  end_time TIME NOT NULL COMMENT 'End time',
  time_slot_label VARCHAR(50) DEFAULT NULL COMMENT 'Display time slot label',
  planned_student_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Planned student count',
  current_student_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Current student count',
  max_student_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Maximum student count',
  status ENUM('scheduled','available','full','ongoing','cancelled') NOT NULL DEFAULT 'scheduled' COMMENT 'Schedule status',
  can_reserve TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether reservation is allowed from this schedule',
  description TEXT COMMENT 'Course description',
  remark TEXT COMMENT 'Remark',
  source_batch_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Source import batch id',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  deleted_at DATETIME(3) DEFAULT NULL COMMENT 'Soft delete time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schedule_no (schedule_no),
  KEY idx_date_lab (schedule_date, lab_id),
  KEY idx_teacher_date (teacher_user_id, schedule_date),
  KEY idx_semester_date (semester_id, schedule_date),
  KEY idx_source_batch_id (source_batch_id),
  CONSTRAINT fk_course_schedules_semester
    FOREIGN KEY (semester_id) REFERENCES academic_semesters (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_course_schedules_lab
    FOREIGN KEY (lab_id) REFERENCES laboratories (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_course_schedules_teacher
    FOREIGN KEY (teacher_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_course_schedules_import_batch
    FOREIGN KEY (source_batch_id) REFERENCES schedule_import_batches (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_course_schedules_weekday CHECK (weekday IS NULL OR weekday BETWEEN 1 AND 7),
  CONSTRAINT chk_course_schedules_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Laboratory course schedules';

CREATE TABLE IF NOT EXISTS reservation_applications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Reservation application primary key',
  application_no VARCHAR(64) NOT NULL COMMENT 'Business reservation application number',
  applicant_user_id BIGINT UNSIGNED NOT NULL COMMENT 'Applicant user id',
  applicant_role ENUM('student','teacher') NOT NULL COMMENT 'Applicant role',
  applicant_no VARCHAR(64) NOT NULL COMMENT 'Applicant account number snapshot',
  applicant_name VARCHAR(50) NOT NULL COMMENT 'Applicant name snapshot',
  applicant_phone VARCHAR(20) DEFAULT NULL COMMENT 'Applicant phone snapshot',
  lab_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked laboratories.id',
  lab_name_snapshot VARCHAR(100) NOT NULL COMMENT 'Laboratory name snapshot',
  schedule_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Linked course_schedules.id when reservation is schedule based',
  reserve_date DATE NOT NULL COMMENT 'Reservation date',
  start_time TIME NOT NULL COMMENT 'Reservation start time',
  end_time TIME NOT NULL COMMENT 'Reservation end time',
  time_slot_label VARCHAR(50) DEFAULT NULL COMMENT 'Display time slot label',
  participant_count INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Participant count',
  application_type VARCHAR(50) DEFAULT NULL COMMENT 'Student reservation type code',
  application_type_name VARCHAR(50) DEFAULT NULL COMMENT 'Student reservation type name',
  title VARCHAR(150) DEFAULT NULL COMMENT 'Student reservation subject',
  purpose TEXT COMMENT 'Usage purpose',
  instructor_name VARCHAR(50) DEFAULT NULL COMMENT 'Instructor or responsible teacher name',
  requirements TEXT COMMENT 'Special requirements',
  emergency_contact_name VARCHAR(50) DEFAULT NULL COMMENT 'Emergency contact name',
  emergency_contact_phone VARCHAR(20) DEFAULT NULL COMMENT 'Emergency contact phone',
  course_name VARCHAR(150) DEFAULT NULL COMMENT 'Teacher reservation course name',
  course_type VARCHAR(50) DEFAULT NULL COMMENT 'Teacher reservation course type',
  remark TEXT COMMENT 'Remark',
  status ENUM('draft','pending','teacher_approved','approved','rejected','cancelled','completed') NOT NULL DEFAULT 'draft' COMMENT 'Current workflow status',
  is_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether usage has completed',
  submitted_at DATETIME(3) DEFAULT NULL COMMENT 'Application submit time',
  teacher_review_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Teacher reviewer user id',
  teacher_review_name VARCHAR(50) DEFAULT NULL COMMENT 'Teacher reviewer name snapshot',
  teacher_review_at DATETIME(3) DEFAULT NULL COMMENT 'Teacher review time',
  teacher_review_comment VARCHAR(500) DEFAULT NULL COMMENT 'Teacher review comment',
  admin_review_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Admin reviewer user id',
  admin_review_name VARCHAR(50) DEFAULT NULL COMMENT 'Admin reviewer name snapshot',
  admin_review_at DATETIME(3) DEFAULT NULL COMMENT 'Admin review time',
  admin_review_comment VARCHAR(500) DEFAULT NULL COMMENT 'Admin review comment',
  reject_reason VARCHAR(500) DEFAULT NULL COMMENT 'Latest reject reason',
  cancelled_at DATETIME(3) DEFAULT NULL COMMENT 'Cancelled time',
  completed_at DATETIME(3) DEFAULT NULL COMMENT 'Completed time',
  source_application_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Original application id for reapply or modify',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  deleted_at DATETIME(3) DEFAULT NULL COMMENT 'Soft delete time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_application_no (application_no),
  KEY idx_applicant_status (applicant_user_id, status),
  KEY idx_lab_date_time (lab_id, reserve_date, start_time, end_time),
  KEY idx_status_submitted (status, submitted_at),
  KEY idx_teacher_review (teacher_review_user_id, teacher_review_at),
  KEY idx_schedule_id (schedule_id),
  KEY idx_admin_review_user_id (admin_review_user_id),
  KEY idx_source_application_id (source_application_id),
  CONSTRAINT fk_reservation_applicant
    FOREIGN KEY (applicant_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_reservation_lab
    FOREIGN KEY (lab_id) REFERENCES laboratories (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_reservation_schedule
    FOREIGN KEY (schedule_id) REFERENCES course_schedules (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_reservation_teacher_reviewer
    FOREIGN KEY (teacher_review_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_reservation_admin_reviewer
    FOREIGN KEY (admin_review_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_reservation_source_application
    FOREIGN KEY (source_application_id) REFERENCES reservation_applications (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_reservation_time CHECK (end_time > start_time),
  CONSTRAINT chk_reservation_participant_count CHECK (participant_count > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Unified student and teacher reservation applications';

CREATE TABLE IF NOT EXISTS reservation_approval_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Reservation approval log primary key',
  application_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked reservation_applications.id',
  stage ENUM('teacher','admin','system') NOT NULL COMMENT 'Workflow stage',
  action ENUM('submit','approve','reject','cancel','complete','reprocess') NOT NULL COMMENT 'Workflow action',
  from_status VARCHAR(32) DEFAULT NULL COMMENT 'Previous status',
  to_status VARCHAR(32) NOT NULL COMMENT 'New status',
  reviewer_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Operator user id; NULL for system actions',
  reviewer_name_snapshot VARCHAR(50) DEFAULT NULL COMMENT 'Operator name snapshot',
  comment VARCHAR(500) DEFAULT NULL COMMENT 'Review comment or operation reason',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Operation time',
  PRIMARY KEY (id),
  KEY idx_application_created (application_id, created_at),
  KEY idx_reviewer_created (reviewer_user_id, created_at),
  CONSTRAINT fk_reservation_approval_logs_application
    FOREIGN KEY (application_id) REFERENCES reservation_applications (id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_reservation_approval_logs_reviewer
    FOREIGN KEY (reviewer_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Reservation workflow approval logs';

CREATE TABLE IF NOT EXISTS reservation_attachments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Reservation attachment primary key',
  application_id BIGINT UNSIGNED NOT NULL COMMENT 'Linked reservation_applications.id',
  file_name VARCHAR(255) NOT NULL COMMENT 'Original file name',
  file_url VARCHAR(500) NOT NULL COMMENT 'File URL or object storage key',
  file_type VARCHAR(80) DEFAULT NULL COMMENT 'MIME type or extension',
  file_size_bytes BIGINT UNSIGNED DEFAULT NULL COMMENT 'File size in bytes',
  uploaded_by BIGINT UNSIGNED DEFAULT NULL COMMENT 'Uploader user id',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Upload time',
  PRIMARY KEY (id),
  KEY idx_application (application_id),
  KEY idx_uploaded_by (uploaded_by),
  CONSTRAINT fk_reservation_attachments_application
    FOREIGN KEY (application_id) REFERENCES reservation_applications (id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_reservation_attachments_uploaded_by
    FOREIGN KEY (uploaded_by) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Reservation attachment files';

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Notification primary key',
  notification_no VARCHAR(64) NOT NULL COMMENT 'Business notification number',
  recipient_user_id BIGINT UNSIGNED NOT NULL COMMENT 'Recipient user id',
  recipient_role ENUM('student','teacher','admin') NOT NULL COMMENT 'Recipient role',
  recipient_account_no VARCHAR(64) NOT NULL COMMENT 'Recipient account number snapshot',
  title VARCHAR(150) NOT NULL COMMENT 'Notification title',
  content TEXT NOT NULL COMMENT 'Notification content',
  type ENUM('info','success','warning','error') NOT NULL DEFAULT 'info' COMMENT 'Notification type',
  related_application_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Related reservation application id',
  is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether notification has been read',
  read_at DATETIME(3) DEFAULT NULL COMMENT 'Read time',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_no (notification_no),
  KEY idx_recipient_read (recipient_user_id, is_read, created_at),
  KEY idx_application (related_application_id),
  CONSTRAINT fk_notifications_recipient
    FOREIGN KEY (recipient_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_notifications_application
    FOREIGN KEY (related_application_id) REFERENCES reservation_applications (id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User notifications';

CREATE TABLE IF NOT EXISTS system_settings (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'System settings primary key',
  system_name VARCHAR(100) NOT NULL COMMENT 'System display name',
  school_name VARCHAR(100) NOT NULL COMMENT 'School display name',
  logo_url VARCHAR(500) DEFAULT NULL COMMENT 'System logo URL',
  reservation_start_time TIME NOT NULL COMMENT 'Daily reservation start time',
  reservation_end_time TIME NOT NULL COMMENT 'Daily reservation end time',
  advance_days INT UNSIGNED NOT NULL DEFAULT 3 COMMENT 'How many days in advance reservation is allowed',
  auto_approval TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether auto approval is enabled',
  reservation_notification_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether reservation notification is enabled',
  approval_notification_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether approval notification is enabled',
  reminder_notification_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Whether reminder notification is enabled',
  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT 'Last updater admin user id',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Created time',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Updated time',
  PRIMARY KEY (id),
  KEY idx_updated_by (updated_by),
  CONSTRAINT fk_system_settings_updated_by
    FOREIGN KEY (updated_by) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_system_settings_time CHECK (reservation_end_time > reservation_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='System-wide settings';

CREATE TABLE IF NOT EXISTS data_backup_records (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Data backup record primary key',
  backup_no VARCHAR(64) NOT NULL COMMENT 'Backup business number',
  backup_type ENUM('labs','schedules','reservations','users','all') NOT NULL COMMENT 'Backup data type',
  backup_name VARCHAR(150) NOT NULL COMMENT 'Backup display name',
  file_url VARCHAR(500) DEFAULT NULL COMMENT 'Backup file URL',
  file_size_bytes BIGINT UNSIGNED DEFAULT NULL COMMENT 'Backup file size in bytes',
  display_size VARCHAR(50) DEFAULT NULL COMMENT 'Display size text',
  status ENUM('processing','success','failed','deleted') NOT NULL DEFAULT 'processing' COMMENT 'Backup status',
  progress TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Backup progress percentage',
  created_by BIGINT UNSIGNED NOT NULL COMMENT 'Admin user who created backup',
  started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Backup start time',
  completed_at DATETIME(3) DEFAULT NULL COMMENT 'Backup complete time',
  restored_at DATETIME(3) DEFAULT NULL COMMENT 'Last restore time',
  deleted_at DATETIME(3) DEFAULT NULL COMMENT 'Deleted time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_backup_no (backup_no),
  KEY idx_type_status (backup_type, status),
  KEY idx_started_at (started_at),
  KEY idx_created_by (created_by),
  CONSTRAINT fk_data_backup_records_created_by
    FOREIGN KEY (created_by) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_data_backup_progress CHECK (progress <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Data backup records';

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Audit log primary key',
  user_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Operator user id',
  role ENUM('admin','teacher','student','anonymous') NOT NULL COMMENT 'Operator role',
  module VARCHAR(50) NOT NULL COMMENT 'Business module name',
  action VARCHAR(50) NOT NULL COMMENT 'Action name',
  target_type VARCHAR(50) DEFAULT NULL COMMENT 'Target entity type',
  target_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'Target entity id',
  detail JSON COMMENT 'Action details in JSON',
  ip_address VARCHAR(64) DEFAULT NULL COMMENT 'Client IP address',
  user_agent VARCHAR(500) DEFAULT NULL COMMENT 'Client user agent',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Log time',
  PRIMARY KEY (id),
  KEY idx_user_created (user_id, created_at),
  KEY idx_module_action_created (module, action, created_at),
  KEY idx_target (target_type, target_id),
  CONSTRAINT fk_audit_logs_user
    FOREIGN KEY (user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='System audit logs';

-- Optional default settings. Run only once after creating the schema if needed.
-- INSERT INTO system_settings (
--   system_name,
--   school_name,
--   logo_url,
--   reservation_start_time,
--   reservation_end_time,
--   advance_days,
--   auto_approval,
--   reservation_notification_enabled,
--   approval_notification_enabled,
--   reminder_notification_enabled
-- ) VALUES (
--   'Lab Reservation Management System',
--   'SCNU',
--   '/static/images/lab.png',
--   '08:30:00',
--   '22:00:00',
--   3,
--   0,
--   1,
--   1,
--   1
-- );

-- ---------------------------------------------------------------------------
-- Mock seed data
-- ---------------------------------------------------------------------------
-- These rows are for local development and UI/API testing.
-- INSERT IGNORE makes the seed mostly repeatable and avoids overwriting real data.
-- Password hashes below are placeholders. Replace them with real bcrypt/argon2 hashes
-- before enabling login against this database.

INSERT IGNORE INTO sys_users (
  id, account_no, role, password_hash, real_name, gender, phone, email, avatar_url,
  status, last_login_at, login_count, agreed_terms_at, created_at, updated_at
) VALUES
  (1, 'admin001', 'admin', '123123', '系统管理员', 'unknown', '13800000001', 'admin@scnu.example', '/static/images/icons/system-icon.png', 'active', '2026-06-11 09:10:00.000', 18, '2026-01-01 00:00:00.000', '2026-01-01 00:00:00.000', '2026-06-11 09:10:00.000'),
  (2, 'T20230001', 'teacher', '123123', '张教授', 'male', '13800138001', 'zhang@scnu.example', '/static/images/icons/teacher-avatar.svg', 'active', '2026-06-11 08:30:00.000', 12, '2026-03-01 10:00:00.000', '2026-03-01 10:00:00.000', '2026-06-11 08:30:00.000'),
  (3, 'T20230002', 'teacher', '123123', '李老师', 'female', '13800138002', 'li@scnu.example', '/static/images/icons/teacher-avatar.svg', 'active', '2026-06-10 16:20:00.000', 9, '2026-03-02 10:00:00.000', '2026-03-02 10:00:00.000', '2026-06-10 16:20:00.000'),
  (4, 'S20230001', 'student', '123123', '王同学', 'male', '13900139001', 'wang.student@scnu.example', '/static/images/icons/user-avatar.svg', 'active', '2026-06-11 08:45:00.000', 22, '2026-03-10 09:00:00.000', '2026-03-10 09:00:00.000', '2026-06-11 08:45:00.000'),
  (5, 'S20230002', 'student', '123123', '赵同学', 'female', '13900139002', 'zhao.student@scnu.example', '/static/images/icons/user-avatar.svg', 'disabled', '2026-06-08 13:20:00.000', 5, '2026-03-11 09:00:00.000', '2026-03-11 09:00:00.000', '2026-06-08 13:20:00.000'),
  (6, 'admin002', 'admin', '123123', '审核管理员', 'unknown', '13800000002', 'reviewer@scnu.example', '/static/images/icons/system-icon.png', 'active', '2026-06-11 10:00:00.000', 7, '2026-01-01 00:00:00.000', '2026-01-01 00:00:00.000', '2026-06-11 10:00:00.000');

INSERT IGNORE INTO student_profiles (
  id, user_id, student_no, college, major, created_at, updated_at
) VALUES
  (1, 4, 'S20230001', '计算机学院', '2023级软件工程', '2026-03-10 09:00:00.000', '2026-06-11 08:45:00.000'),
  (2, 5, 'S20230002', '经济与管理学院', '2023级信息管理与信息系统', '2026-03-11 09:00:00.000', '2026-06-08 13:20:00.000');

INSERT IGNORE INTO teacher_profiles (
  id, user_id, teacher_no, college, department, position_title, created_at, updated_at
) VALUES
  (1, 2, 'T20230001', '计算机学院', '软件工程系', '教授', '2026-03-01 10:00:00.000', '2026-06-11 08:30:00.000'),
  (2, 3, 'T20230002', '经济与管理学院', '信息管理系', '讲师', '2026-03-02 10:00:00.000', '2026-06-10 16:20:00.000');

INSERT IGNORE INTO teacher_registration_applications (
  id, application_no, teacher_no, real_name, gender, college, department, position_title,
  phone, email, password_hash, id_card_front_url, id_card_back_url, teacher_card_image_url,
  status, reject_reason, reviewer_user_id, applied_at, reviewed_at, created_at, updated_at
) VALUES
  (1, 'TRA20260611001', 'T20230003', '陈老师', 'female', '外国语学院', '法语系', '讲师', '13700137003', 'chen@scnu.example', '123123', '/mock/id-card-front-chen.png', '/mock/id-card-back-chen.png', '/mock/teacher-card-chen.png', 'pending', NULL, NULL, '2026-06-10 09:18:33.000', NULL, '2026-06-10 09:18:33.000', '2026-06-10 09:18:33.000'),
  (2, 'TRA20260611002', 'T20230004', '刘教授', 'male', '数学科学学院', '应用数学系', '教授', '13700137004', 'liu@scnu.example', '123123', '/mock/id-card-front-liu.png', '/mock/id-card-back-liu.png', '/mock/teacher-card-liu.png', 'approved', NULL, 6, '2026-06-09 14:35:22.000', '2026-06-10 10:15:30.000', '2026-06-09 14:35:22.000', '2026-06-10 10:15:30.000'),
  (3, 'TRA20260611003', 'T20230005', '孙老师', 'male', '物理学院', '实验教学中心', '实验师', '13700137005', 'sun@scnu.example', '123123', '/mock/id-card-front-sun.png', '/mock/id-card-back-sun.png', '/mock/teacher-card-sun.png', 'rejected', '教师证明材料不清晰，请重新上传。', 6, '2026-06-08 16:42:18.000', '2026-06-09 11:20:45.000', '2026-06-08 16:42:18.000', '2026-06-09 11:20:45.000');

INSERT IGNORE INTO laboratories (
  id, lab_code, name, location, capacity, equipment, image_url, status,
  created_by, updated_by, created_at, updated_at
) VALUES
  (1, 'lab1', '国际课程实验室', '综合楼东A301', 65, '可移动组合桌椅60套，纳米投影书写墙，话筒2支，服务器2台', '/static/images/东A301.png', 'active', 1, 1, '2026-01-05 09:00:00.000', '2026-06-01 09:00:00.000'),
  (2, 'lab2', 'IBC实验中心', '综合楼西A302', 100, '开放公共自习室，可容纳百人，打印机、投影仪等设施齐全', '/static/images/西A302.jpg', 'active', 1, 1, '2026-01-05 09:05:00.000', '2026-06-01 09:05:00.000'),
  (3, 'lab3', '互联网+新商科实验室', '综合楼西A303', 80, '80个智能工位，配备翻盖式电脑，seewo 2台', '/static/images/西A303.png', 'active', 1, 1, '2026-01-05 09:10:00.000', '2026-06-01 09:10:00.000'),
  (4, 'lab4', '法语实验室', '综合楼西A305', 60, '语音教学设备、录播系统、投影仪', '/static/images/天空实验室.png', 'maintenance', 1, 6, '2026-01-05 09:15:00.000', '2026-06-10 16:00:00.000'),
  (5, 'lab5', '402实验室', '综合楼西A402', 80, '开发环境、测试工具、标准配置电脑', '/static/images/天空实验室.png', 'active', 1, 1, '2026-01-05 09:20:00.000', '2026-06-01 09:20:00.000');

INSERT IGNORE INTO academic_semesters (
  id, semester_code, semester_name, academic_year, term_no, start_date, end_date, status, created_at, updated_at
) VALUES
  (1, '2025-2026-2', '2025-2026学年第二学期', '2025-2026', 2, '2026-02-23', '2026-07-05', 'active', '2026-01-01 00:00:00.000', '2026-06-11 00:00:00.000'),
  (2, '2026-2027-1', '2026-2027学年第一学期', '2026-2027', 1, '2026-09-01', '2027-01-18', 'inactive', '2026-06-01 00:00:00.000', '2026-06-01 00:00:00.000');

INSERT IGNORE INTO schedule_import_batches (
  id, batch_no, semester_id, file_name, file_size_bytes, file_path, status, progress,
  success_count, failure_count, error_details, imported_by, started_at, finished_at, created_at, updated_at
) VALUES
  (1, 'SIB20260611001', 1, '2025-2026-2学期课表.xlsx', 262144, '/uploads/schedules/2025-2026-2.xlsx', 'success', 100, 128, 0, NULL, 1, '2026-06-01 09:00:00.000', '2026-06-01 09:02:35.000', '2026-06-01 09:00:00.000', '2026-06-01 09:02:35.000'),
  (2, 'SIB20260611002', 1, '错误示例课表.xlsx', 98304, '/uploads/schedules/error-sample.xlsx', 'failed', 100, 0, 3, '第5行教室信息不完整；第8行时间格式错误。', 1, '2026-06-08 14:00:00.000', '2026-06-08 14:01:20.000', '2026-06-08 14:00:00.000', '2026-06-08 14:01:20.000');

INSERT IGNORE INTO schedule_export_tasks (
  id, task_no, semester_id, export_format, include_rooms, include_teachers, include_students,
  file_url, status, error_message, created_by, created_at, finished_at
) VALUES
  (1, 'SET20260611001', 1, 'excel', 1, 1, 0, '/exports/schedules/2025-2026-2.xlsx', 'success', NULL, 1, '2026-06-09 15:00:00.000', '2026-06-09 15:00:25.000');

INSERT IGNORE INTO course_schedules (
  id, schedule_no, semester_id, lab_id, teacher_user_id, teacher_name_snapshot, course_name,
  course_type, schedule_date, weekday, start_time, end_time, time_slot_label,
  planned_student_count, current_student_count, max_student_count, status, can_reserve,
  description, remark, source_batch_id, created_at, updated_at
) VALUES
  (1, 'CS20260611001', 1, 3, 2, '张教授', '数据结构与算法', '理论课', '2026-06-12', 5, '08:00:00', '09:50:00', '08:00-09:50', 30, 25, 30, 'available', 1, '学习基本的数据结构和算法设计方法。', '导入课表样例', 1, '2026-06-01 09:05:00.000', '2026-06-01 09:05:00.000'),
  (2, 'CS20260611002', 1, 1, 3, '李老师', '计算机网络', '实验课', '2026-06-12', 5, '10:00:00', '11:50:00', '10:00-11:50', 30, 30, 30, 'full', 0, '计算机网络协议和网络安全实验。', '人数已满', 1, '2026-06-01 09:05:30.000', '2026-06-01 09:05:30.000'),
  (3, 'CS20260611003', 1, 4, 2, '张教授', '软件工程', '实践课', '2026-06-12', 5, '14:00:00', '15:50:00', '14:00-15:50', 25, 20, 25, 'ongoing', 0, '软件开发生命周期实践。', '维修期间仅供查看', 1, '2026-06-01 09:06:00.000', '2026-06-10 16:00:00.000'),
  (4, 'CS20260611004', 1, 5, 3, '李老师', '数据库系统', '实验课', '2026-06-13', 6, '19:00:00', '20:50:00', '19:00-20:50', 28, 18, 28, 'scheduled', 1, '数据库建模与查询优化实验。', NULL, 1, '2026-06-01 09:06:30.000', '2026-06-01 09:06:30.000');

INSERT IGNORE INTO reservation_applications (
  id, application_no, applicant_user_id, applicant_role, applicant_no, applicant_name, applicant_phone,
  lab_id, lab_name_snapshot, schedule_id, reserve_date, start_time, end_time, time_slot_label,
  participant_count, application_type, application_type_name, title, purpose, instructor_name,
  requirements, emergency_contact_name, emergency_contact_phone, course_name, course_type, remark,
  status, is_completed, submitted_at, teacher_review_user_id, teacher_review_name, teacher_review_at,
  teacher_review_comment, admin_review_user_id, admin_review_name, admin_review_at, admin_review_comment,
  reject_reason, cancelled_at, completed_at, source_application_id, created_at, updated_at
) VALUES
  (1, 'RA20260611001', 4, 'student', 'S20230001', '王同学', '13900139001', 1, '国际课程实验室', NULL, '2026-06-15', '08:00:00', '09:50:00', '08:00-09:50', 6, 'course', '课程实验', '课程设计小组讨论', '需要使用实验室进行课程设计讨论和原型验证。', '张教授', '投影仪、白板', '王同学', '13900139001', NULL, NULL, NULL, 'pending', 0, '2026-06-11 09:00:00.000', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-11 09:00:00.000', '2026-06-11 09:00:00.000'),
  (2, 'RA20260611002', 4, 'student', 'S20230001', '王同学', '13900139001', 3, '互联网+新商科实验室', 1, '2026-06-16', '14:00:00', '15:50:00', '14:00-15:50', 10, 'research', '科研项目', '机器学习算法实验', '需要 GPU 工作站完成模型训练。', '张教授', 'GPU 工作站、投影仪', '王同学', '13900139001', NULL, NULL, NULL, 'teacher_approved', 0, '2026-06-10 14:20:00.000', 2, '张教授', '2026-06-10 16:00:00.000', '研究内容合理，同意提交管理员终审。', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-10 14:20:00.000', '2026-06-10 16:00:00.000'),
  (3, 'RA20260611003', 2, 'teacher', 'T20230001', '张教授', '13800138001', 5, '402实验室', 4, '2026-06-17', '10:00:00', '11:50:00', '10:00-11:50', 32, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '数据库系统实验课', '实验课', '需要提前安装 MySQL 和建模工具。', 'approved', 0, '2026-06-09 10:30:00.000', NULL, NULL, NULL, NULL, 6, '审核管理员', '2026-06-09 15:15:00.000', '课程安排明确，审批通过。', NULL, NULL, NULL, NULL, '2026-06-09 10:30:00.000', '2026-06-09 15:15:00.000'),
  (4, 'RA20260611004', 5, 'student', 'S20230002', '赵同学', '13900139002', 4, '法语实验室', NULL, '2026-06-18', '19:00:00', '20:50:00', '19:00-20:50', 8, 'activity', '学术活动', '社团分享会', '计划举办小型语言文化分享活动。', '李老师', '录播设备', '赵同学', '13900139002', NULL, NULL, NULL, 'rejected', 0, '2026-06-08 11:20:00.000', 3, '李老师', '2026-06-08 15:30:00.000', '该实验室设备维护中，请更换实验室。', NULL, NULL, NULL, NULL, '该实验室设备维护中，请更换实验室。', NULL, NULL, NULL, '2026-06-08 11:20:00.000', '2026-06-08 15:30:00.000'),
  (5, 'RA20260611005', 4, 'student', 'S20230001', '王同学', '13900139001', 2, 'IBC实验中心', NULL, '2026-06-01', '16:00:00', '17:50:00', '16:00-17:50', 12, 'competition', '竞赛培训', '算法竞赛训练', '团队进行赛前模拟训练。', '张教授', '公共自习区、投影仪', '王同学', '13900139001', NULL, NULL, NULL, 'completed', 1, '2026-05-28 10:10:00.000', 2, '张教授', '2026-05-28 11:00:00.000', '同意使用。', 6, '审核管理员', '2026-05-28 14:00:00.000', '审批通过。', NULL, NULL, '2026-06-01 18:00:00.000', NULL, '2026-05-28 10:10:00.000', '2026-06-01 18:00:00.000');

INSERT IGNORE INTO reservation_approval_logs (
  id, application_id, stage, action, from_status, to_status, reviewer_user_id,
  reviewer_name_snapshot, comment, created_at
) VALUES
  (1, 1, 'system', 'submit', NULL, 'pending', 4, '王同学', '学生提交预约申请。', '2026-06-11 09:00:00.000'),
  (2, 2, 'system', 'submit', NULL, 'pending', 4, '王同学', '学生提交科研项目预约。', '2026-06-10 14:20:00.000'),
  (3, 2, 'teacher', 'approve', 'pending', 'teacher_approved', 2, '张教授', '研究内容合理，同意提交管理员终审。', '2026-06-10 16:00:00.000'),
  (4, 3, 'system', 'submit', NULL, 'pending', 2, '张教授', '教师提交课程预约。', '2026-06-09 10:30:00.000'),
  (5, 3, 'admin', 'approve', 'pending', 'approved', 6, '审核管理员', '课程安排明确，审批通过。', '2026-06-09 15:15:00.000'),
  (6, 4, 'system', 'submit', NULL, 'pending', 5, '赵同学', '学生提交学术活动预约。', '2026-06-08 11:20:00.000'),
  (7, 4, 'teacher', 'reject', 'pending', 'rejected', 3, '李老师', '该实验室设备维护中，请更换实验室。', '2026-06-08 15:30:00.000'),
  (8, 5, 'system', 'submit', NULL, 'pending', 4, '王同学', '学生提交竞赛培训预约。', '2026-05-28 10:10:00.000'),
  (9, 5, 'teacher', 'approve', 'pending', 'teacher_approved', 2, '张教授', '同意使用。', '2026-05-28 11:00:00.000'),
  (10, 5, 'admin', 'approve', 'teacher_approved', 'approved', 6, '审核管理员', '审批通过。', '2026-05-28 14:00:00.000'),
  (11, 5, 'system', 'complete', 'approved', 'completed', NULL, '系统', '预约日期已结束，自动标记完成。', '2026-06-01 18:00:00.000');

INSERT IGNORE INTO reservation_attachments (
  id, application_id, file_name, file_url, file_type, file_size_bytes, uploaded_by, created_at
) VALUES
  (1, 1, '课程设计计划.docx', '/uploads/reservations/RA20260611001-plan.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 524288, 4, '2026-06-11 09:00:10.000'),
  (2, 3, '数据库实验安排.pdf', '/uploads/reservations/RA20260611003-schedule.pdf', 'application/pdf', 786432, 2, '2026-06-09 10:30:15.000');

INSERT IGNORE INTO notifications (
  id, notification_no, recipient_user_id, recipient_role, recipient_account_no, title, content,
  type, related_application_id, is_read, read_at, created_at
) VALUES
  (1, 'N20260611001', 4, 'student', 'S20230001', '教师审核通过', '您的互联网+新商科实验室预约已通过教师审核，等待管理员终审。', 'success', 2, 0, NULL, '2026-06-10 16:00:10.000'),
  (2, 'N20260611002', 2, 'teacher', 'T20230001', '预约审批通过', '您的 402实验室 课程预约已由管理员审核通过。', 'success', 3, 1, '2026-06-09 16:00:00.000', '2026-06-09 15:15:10.000'),
  (3, 'N20260611003', 5, 'student', 'S20230002', '预约申请被驳回', '您的法语实验室预约因设备维护被驳回，请更换实验室后重新提交。', 'warning', 4, 0, NULL, '2026-06-08 15:30:10.000'),
  (4, 'N20260611004', 4, 'student', 'S20230001', '预约已完成', '您的 IBC实验中心 预约已完成使用。', 'info', 5, 1, '2026-06-01 18:30:00.000', '2026-06-01 18:00:10.000');

INSERT IGNORE INTO system_settings (
  id, system_name, school_name, logo_url, reservation_start_time, reservation_end_time,
  advance_days, auto_approval, reservation_notification_enabled, approval_notification_enabled,
  reminder_notification_enabled, updated_by, created_at, updated_at
) VALUES
  (1, '实验室预约管理系统', 'SCNU', '/static/images/天空实验室.png', '08:30:00', '22:00:00', 3, 0, 1, 1, 1, 1, '2026-01-01 00:00:00.000', '2026-06-11 09:00:00.000');

INSERT IGNORE INTO data_backup_records (
  id, backup_no, backup_type, backup_name, file_url, file_size_bytes, display_size,
  status, progress, created_by, started_at, completed_at, restored_at, deleted_at
) VALUES
  (1, 'BKP20260611001', 'all', '全部数据备份', '/backups/all-20260611.sql.gz', 5452595, '5.2MB', 'success', 100, 1, '2026-06-11 02:00:00.000', '2026-06-11 02:01:30.000', NULL, NULL),
  (2, 'BKP20260610001', 'labs', '实验室数据备份', '/backups/labs-20260610.sql.gz', 1887436, '1.8MB', 'success', 100, 1, '2026-06-10 02:00:00.000', '2026-06-10 02:00:40.000', NULL, NULL),
  (3, 'BKP20260609001', 'schedules', '课表数据备份', '/backups/schedules-20260609.sql.gz', 2411725, '2.3MB', 'success', 100, 1, '2026-06-09 02:00:00.000', '2026-06-09 02:00:50.000', NULL, NULL);

INSERT IGNORE INTO audit_logs (
  id, user_id, role, module, action, target_type, target_id, detail, ip_address, user_agent, created_at
) VALUES
  (1, 1, 'admin', 'auth', 'login', 'sys_users', 1, JSON_OBJECT('result', 'success'), '127.0.0.1', 'mock-client', '2026-06-11 09:10:00.000'),
  (2, 1, 'admin', 'laboratory', 'create', 'laboratories', 5, JSON_OBJECT('labCode', 'lab5', 'name', '402实验室'), '127.0.0.1', 'mock-client', '2026-01-05 09:20:00.000'),
  (3, 2, 'teacher', 'reservation', 'approve', 'reservation_applications', 2, JSON_OBJECT('fromStatus', 'pending', 'toStatus', 'teacher_approved'), '127.0.0.1', 'mock-client', '2026-06-10 16:00:00.000'),
  (4, 6, 'admin', 'reservation', 'approve', 'reservation_applications', 3, JSON_OBJECT('fromStatus', 'pending', 'toStatus', 'approved'), '127.0.0.1', 'mock-client', '2026-06-09 15:15:00.000'),
  (5, 1, 'admin', 'schedule', 'import', 'schedule_import_batches', 1, JSON_OBJECT('status', 'success', 'successCount', 128), '127.0.0.1', 'mock-client', '2026-06-01 09:02:35.000');
