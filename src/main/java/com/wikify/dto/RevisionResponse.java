package com.wikify.dto;

import com.wikify.entity.Revision;

import java.time.LocalDateTime;

public record RevisionResponse(Long id, String title, String contentMarkdown,
                               Long documentId, String authorName, LocalDateTime createdAt) {

    public static RevisionResponse from(Revision revision) {
        return new RevisionResponse(
                revision.getId(),
                revision.getTitle(),
                revision.getContentMarkdown(),
                revision.getDocument().getId(),
                revision.getAuthor().getName(),
                revision.getCreatedAt());
    }
}
