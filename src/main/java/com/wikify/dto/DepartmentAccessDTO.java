package com.wikify.dto;

import com.wikify.entity.DepartmentMembership;
import com.wikify.entity.enums.DepartmentRole;

public record DepartmentAccessDTO(Long id, String name, String slug, DepartmentRole role) {

    public static DepartmentAccessDTO from(DepartmentMembership membership) {
        return new DepartmentAccessDTO(
                membership.getDepartment().getId(),
                membership.getDepartment().getName(),
                membership.getDepartment().getSlug(),
                membership.getRole());
    }
}
