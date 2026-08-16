package com.wikify.controller;

import com.wikify.dto.MediaResponse;
import com.wikify.entity.User;
import com.wikify.services.MediaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("departmentId") Long departmentId,
            @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(mediaService.upload(file, departmentId, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping(value = "/{id}", headers = "Range")
    public ResponseEntity<ResourceRegion> getPartialMedia(
            @PathVariable UUID id,
            @RequestHeader HttpHeaders headers,
            @AuthenticationPrincipal User user) {
        try {
            return mediaService.getPartialMedia(headers, id, user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMedia(@PathVariable UUID id, @RequestHeader HttpHeaders headers, @AuthenticationPrincipal User user){
        try {
            return mediaService.getMedia(headers, id, user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
