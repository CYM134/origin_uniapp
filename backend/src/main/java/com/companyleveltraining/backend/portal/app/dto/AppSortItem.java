package com.companyleveltraining.backend.portal.app.dto;

/** 应用排序项，用于批量调整应用排序号。 */
public record AppSortItem(Long id, Integer sortOrder) {
}
