package com.wikify.dto;

import com.wikify.entity.DepartmentApprovalPublish;

import java.time.LocalDateTime;

public record DepartmentApproveResponse(
        Long departmentId,
        String departmentName,
        boolean approvePublish,
        String changedByName,
        LocalDateTime changedAt) {

    public static DepartmentApproveResponse from(DepartmentApprovalPublish config) {
        return new DepartmentApproveResponse(
                config.getDepartment().getId(),
                config.getDepartment().getName(),
                config.isApprovePublish(),
                config.getChangedBy() != null ? config.getChangedBy().getName() : null,
                config.getChangedAt());
    }
}
