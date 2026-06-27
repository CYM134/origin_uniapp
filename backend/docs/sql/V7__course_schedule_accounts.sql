-- V7__course_schedule_accounts.sql
-- Bind course schedules to student accounts so teacher/student schedule pages can show "my schedule".

CREATE TABLE IF NOT EXISTS course_schedule_students (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'course schedule student relation id',
  schedule_id BIGINT UNSIGNED NOT NULL COMMENT 'course_schedules.id',
  student_user_id BIGINT UNSIGNED NOT NULL COMMENT 'sys_users.id of student',
  student_account_no VARCHAR(64) NOT NULL COMMENT 'student account snapshot',
  student_name_snapshot VARCHAR(50) NOT NULL COMMENT 'student name snapshot',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_schedule_student (schedule_id, student_user_id),
  KEY idx_student_date (student_user_id, schedule_id),
  CONSTRAINT fk_course_schedule_students_schedule
    FOREIGN KEY (schedule_id) REFERENCES course_schedules (id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_course_schedule_students_student
    FOREIGN KEY (student_user_id) REFERENCES sys_users (id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='student schedule assignments';

SET @demo_semester_id := (
  SELECT id
  FROM academic_semesters
  ORDER BY CASE WHEN status = 'active' THEN 0 ELSE 1 END, start_date DESC, id DESC
  LIMIT 1
);
SET @demo_teacher_id := (
  SELECT id FROM sys_users
  WHERE role = 'teacher' AND account_no = 'T20230001' AND deleted_at IS NULL
  LIMIT 1
);
SET @demo_teacher_name := (
  SELECT real_name FROM sys_users
  WHERE id = @demo_teacher_id
  LIMIT 1
);
SET @demo_student_id := (
  SELECT id FROM sys_users
  WHERE role = 'student' AND account_no = 'S20230001' AND deleted_at IS NULL
  LIMIT 1
);
SET @demo_student_name := (
  SELECT real_name FROM sys_users
  WHERE id = @demo_student_id
  LIMIT 1
);
SET @demo_lab_id := (
  SELECT id FROM laboratories
  WHERE deleted_at IS NULL AND status <> 'deleted'
  ORDER BY id
  LIMIT 1
);

INSERT IGNORE INTO course_schedules (
  schedule_no, semester_id, lab_id, teacher_user_id, teacher_name_snapshot, course_name,
  course_type, schedule_date, weekday, start_time, end_time, time_slot_label,
  planned_student_count, current_student_count, max_student_count, status, can_reserve,
  description, remark
)
SELECT
  'CS-DEMO-MY-SCHEDULE-01', @demo_semester_id, @demo_lab_id, @demo_teacher_id, @demo_teacher_name,
  '数据结构与算法', '理论课', CURDATE(), WEEKDAY(CURDATE()) + 1, '08:00:00', '09:50:00', '08:00-09:50',
  1, 1, 40, 'scheduled', 0, '用于展示师生端课表模块的示例课程。', '系统示例课表'
WHERE @demo_semester_id IS NOT NULL AND @demo_lab_id IS NOT NULL AND @demo_teacher_id IS NOT NULL;

INSERT IGNORE INTO course_schedules (
  schedule_no, semester_id, lab_id, teacher_user_id, teacher_name_snapshot, course_name,
  course_type, schedule_date, weekday, start_time, end_time, time_slot_label,
  planned_student_count, current_student_count, max_student_count, status, can_reserve,
  description, remark
)
SELECT
  'CS-DEMO-MY-SCHEDULE-02', @demo_semester_id, @demo_lab_id, @demo_teacher_id, @demo_teacher_name,
  '数据库系统实验', '实验课', DATE_ADD(CURDATE(), INTERVAL 1 DAY), WEEKDAY(DATE_ADD(CURDATE(), INTERVAL 1 DAY)) + 1,
  '14:00:00', '15:50:00', '14:00-15:50',
  1, 1, 40, 'scheduled', 0, '用于展示师生端课表模块的示例课程。', '系统示例课表'
WHERE @demo_semester_id IS NOT NULL AND @demo_lab_id IS NOT NULL AND @demo_teacher_id IS NOT NULL;

INSERT IGNORE INTO course_schedule_students (
  schedule_id, student_user_id, student_account_no, student_name_snapshot
)
SELECT cs.id, @demo_student_id, 'S20230001', @demo_student_name
FROM course_schedules cs
WHERE cs.schedule_no IN ('CS-DEMO-MY-SCHEDULE-01', 'CS-DEMO-MY-SCHEDULE-02')
  AND @demo_student_id IS NOT NULL;
