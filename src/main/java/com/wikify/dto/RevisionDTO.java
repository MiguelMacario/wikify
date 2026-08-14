package com.wikify.dto;

import com.wikify.entity.Revision;

import java.time.LocalDateTime;

public record RevisionDTO(Long id, String title, String authorName, LocalDateTime createdAt) {

    public static RevisionDTO from(Revision revision) {
        return new RevisionDTO(
                revision.getId(),
                revision.getTitle(),
                revision.getAuthor().getName(),
                revision.getCreatedAt());
    }
}
