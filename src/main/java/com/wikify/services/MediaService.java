package com.wikify.services;

import com.wikify.dto.MediaResponse;
import com.wikify.entity.Department;
import com.wikify.entity.Media;
import com.wikify.entity.User;
import com.wikify.media.MediaContent;
import com.wikify.media.MediaStorage;
import com.wikify.media.MediaTypeDetector;
import com.wikify.repositories.DepartmentRepository;
import com.wikify.repositories.MediaRepository;
import com.wikify.security.DepartmentSecurity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaStorage storage;
    private final MediaRepository mediaRepository;
    private final DepartmentSecurity departmentSecurity;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public MediaResponse upload(MultipartFile file, Long departmentId, User user) {

        if (!departmentSecurity.canContribute(user, departmentId)) {
            throw new AccessDeniedException("Você não contribui com esse departamento");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado"));

        String storageKey;
        String contentType;

        try (InputStream content = new BufferedInputStream(file.getInputStream())) {

            content.mark(MediaTypeDetector.HEADER_BYTES);
            byte[] header = content.readNBytes(MediaTypeDetector.HEADER_BYTES);
            content.reset();

            contentType = MediaTypeDetector.detect(header)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tipo de arquivo não permitido. Aceitos: PNG, JPEG, GIF, WebP, MP4 e WebM."));

            storageKey = storage.store(content, contentType);

        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o arquivo enviado", e);
        }

        Media media = mediaRepository.save(Media.builder()
                .storageKey(storageKey)
                .provider(storage.providerName())
                .contentType(contentType)
                .sizeBytes(file.getSize())
                .originalFilename(file.getOriginalFilename())
                .department(department)
                .uploadedBy(user)
                .build());

        return MediaResponse.from(media);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ResourceRegion> getPartialMedia(HttpHeaders headers, UUID id, User user) {
        long left = 1024 * 1024;
        MediaContent mediaContent = openReadable(id, user);
        long size = mediaContent.sizeBytes();
        List<HttpRange> ranges = headers.getRange();
        MediaType type = MediaType.parseMediaType(mediaContent.contentType());


        HttpRange range = ranges.get(0);
        long inicio = range.getRangeStart(size);
        long fim = range.getRangeEnd(size);
        long comprimento = Math.min(left, fim - inicio + 1);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(type)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(new ResourceRegion(mediaContent.resource(), inicio, comprimento));

    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getMedia(HttpHeaders headers, UUID id, User user){
        MediaContent mediaContent = openReadable(id, user);
        MediaType type = MediaType.parseMediaType(mediaContent.contentType());
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(mediaContent.resource());
    }

    private MediaContent openReadable(UUID id, User user) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mídia não encontrada"));

        if (!departmentSecurity.canRead(user, media.getDepartment().getId())) {
            throw new EntityNotFoundException("Mídia não encontrada");
        }

        return new MediaContent(
                storage.open(media.getStorageKey()),
                media.getContentType(),
                media.getSizeBytes());
    }
}
