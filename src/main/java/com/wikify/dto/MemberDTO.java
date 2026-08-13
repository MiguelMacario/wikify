package com.wikify.dto;

import com.wikify.entity.enums.DepartmentRole;

public record MemberDTO(Long userId, DepartmentRole role) {
}
