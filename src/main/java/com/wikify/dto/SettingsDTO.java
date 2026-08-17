package com.wikify.dto;

import com.wikify.entity.enums.Theme;

import java.util.UUID;

public record SettingsDTO(String appName, UUID logoMediaId, Theme theme, String primaryColor,
                          String homeTitle, String homeContent) {
}
