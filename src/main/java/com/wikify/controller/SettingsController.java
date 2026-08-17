package com.wikify.controller;

import com.wikify.dto.SettingsDTO;
import com.wikify.dto.SettingsResponse;
import com.wikify.entity.User;
import com.wikify.media.MediaContent;
import com.wikify.services.SettingsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings() {
        try {
            return ResponseEntity.ok(settingsService.getSettings());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/logo")
    public ResponseEntity<Resource> getLogo() {
        try {
            MediaContent logo = settingsService.openLogo();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(logo.contentType()))
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                    .body(logo.resource());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<SettingsResponse> updateSettings(@RequestBody SettingsDTO settingsDTO,
                                                           @AuthenticationPrincipal User user) {
        try {
            settingsService.updateSettings(settingsDTO, user);
            return ResponseEntity.ok(settingsService.getSettings());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
