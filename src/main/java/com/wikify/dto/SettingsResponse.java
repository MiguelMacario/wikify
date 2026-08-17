package com.wikify.dto;

import com.wikify.entity.Settings;
import com.wikify.entity.enums.Theme;

import java.time.LocalDateTime;
import java.util.UUID;

public record SettingsResponse(String appName, UUID logoMediaId, String logoUrl,
                               Theme theme, String primaryColor,
                               String homeTitle, String homeContent, LocalDateTime updatedAt) {

    public static SettingsResponse from(Settings settings) {
        return new SettingsResponse(
                settings.getAppName(),
                settings.getLogoMediaId(),
                settings.getLogoMediaId() == null ? null : "/settings/logo",
                settings.getTheme(),
                settings.getPrimaryColor(),
                settings.getHomeTitle(),
                settings.getHomeContent(),
                settings.getUpdatedAt());
    }
}
