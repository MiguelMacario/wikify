package com.wikify.dto;

import com.wikify.entity.Media;

import java.util.UUID;


public record MediaResponse(UUID id, String url, String contentType, long sizeBytes) {

    public static MediaResponse from(Media media) {
        return new MediaResponse(
                media.getId(),
                "/media/" + media.getId(),
                media.getContentType(),
                media.getSizeBytes());
    }
}
