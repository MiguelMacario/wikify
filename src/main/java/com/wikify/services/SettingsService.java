package com.wikify.services;

import com.wikify.content.MarkdownGuard;
import com.wikify.dto.SettingsDTO;
import com.wikify.dto.SettingsResponse;
import com.wikify.entity.Media;
import com.wikify.entity.Settings;
import com.wikify.entity.User;
import com.wikify.media.MediaContent;
import com.wikify.media.MediaStorage;
import com.wikify.repositories.MediaRepository;
import com.wikify.repositories.SettingsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private static final long ID = 1L;

    private static final Pattern COLOR_HEX = Pattern.compile("^#[0-9a-fA-F]{6}$");
    private static final int MAX_NAME_SIZE = 60;

    private final SettingsRepository settingsRepository;
    private final MediaRepository mediaRepository;
    private final MediaStorage mediaStorage;

    @Transactional(readOnly = true)
    public SettingsResponse getSettings() {
        return SettingsResponse.from(load());
    }

    @Transactional
    public void updateSettings(SettingsDTO dto, User user) {
        validate(dto);

        Settings settings = load();
        settings.setAppName(dto.appName().trim());
        settings.setLogoMediaId(dto.logoMediaId());
        settings.setTheme(dto.theme());
        settings.setPrimaryColor(dto.primaryColor());
        settings.setHomeTitle(dto.homeTitle());
        settings.setHomeContent(dto.homeContent());
        settings.setUpdatedBy(user);
    }

    @Transactional(readOnly = true)
    public MediaContent openLogo() {
        UUID logoId = load().getLogoMediaId();
        if (logoId == null) {
            throw new EntityNotFoundException("Nenhum logo configurado");
        }

        Media logo = mediaRepository.findById(logoId)
                .orElseThrow(() -> new EntityNotFoundException("Logo não encontrado"));

        return new MediaContent(
                mediaStorage.open(logo.getStorageKey()),
                logo.getContentType(),
                logo.getSizeBytes());
    }

    private Settings load() {
        return settingsRepository.findById(ID)
                .orElseThrow(() -> new EntityNotFoundException("Configuração não encontrada"));
    }

    private void validate(SettingsDTO dto) {
        if (dto.appName() == null || dto.appName().isBlank()) {
            throw new IllegalArgumentException("O nome da aplicação é obrigatório.");
        }
        if (dto.appName().trim().length() > MAX_NAME_SIZE) {
            throw new IllegalArgumentException(
                    "O nome da aplicação passa de " + MAX_NAME_SIZE + " caracteres.");
        }
        if (dto.theme() == null) {
            throw new IllegalArgumentException("O tema é obrigatório.");
        }

        if (dto.primaryColor() != null && !COLOR_HEX.matcher(dto.primaryColor()).matches()) {
            throw new IllegalArgumentException("A cor precisa estar no formato #RRGGBB.");
        }

        if (dto.logoMediaId() != null && !mediaRepository.existsById(dto.logoMediaId())) {
            throw new IllegalArgumentException("A mídia informada para o logo não existe.");
        }
        MarkdownGuard.rejectDangerousHtml(dto.homeContent());
    }
}
