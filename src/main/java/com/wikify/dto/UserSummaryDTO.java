package com.wikify.dto;

import com.wikify.entity.User;

public record UserSummaryDTO(Long id, String name, String login, String email) {

    public static UserSummaryDTO from(User user) {
        return new UserSummaryDTO(user.getId(), user.getName(), user.getLogin(), user.getEmail());
    }
}
