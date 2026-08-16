package com.wikify.dto;

import com.wikify.entity.DepartmentMembership;
import com.wikify.entity.enums.DepartmentRole;

public record MemberResponse(Long userId, String name, String login, DepartmentRole role) {

    public static MemberResponse from(DepartmentMembership membership) {
        return new MemberResponse(
                membership.getUser().getId(),
                membership.getUser().getName(),
                membership.getUser().getLogin(),
                membership.getRole());
    }
}
