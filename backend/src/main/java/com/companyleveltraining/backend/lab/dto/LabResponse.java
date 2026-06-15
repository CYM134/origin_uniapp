package com.companyleveltraining.backend.lab.dto;

/**
 * 实验室响应对象。字段名对齐前端各页使用的 mock 形状：
 * - admin-lab-management 用 id/name/location/equipment/image
 * - teacher-reservation-apply 用 capacity
 * - student-reservation-apply 用 maxStudents
 * 因此同时提供 capacity 与 maxStudents（值相同），减少前端改动。
 */
public record LabResponse(
    Long id,
    String labCode,
    String name,
    String location,
    String equipment,
    String image,
    Integer capacity,
    Integer maxStudents,
    String status,
    String statusText
) {
}
