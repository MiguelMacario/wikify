package com.wikify.dto;

import com.wikify.entity.enums.SystemRole;

import java.util.List;

public record LoginResponseDTO(String token, SystemRole systemRole, List<DepartmentAccessDTO> departments, Long userId, String userName) {
}
