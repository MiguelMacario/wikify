package com.wikify.dto;

public record CreateDepartmentRequest(Long managerId, String name, String slug) {
}
