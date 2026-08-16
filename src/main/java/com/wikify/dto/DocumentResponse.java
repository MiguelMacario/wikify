package com.wikify.dto;

import com.wikify.entity.Document;

import java.time.LocalDateTime;

public record DocumentResponse(Long id, String title, String content, String slug, String path,
                               Long departmentId, Long parentId, int position, String status,
                               String authorName, LocalDateTime publishedAt) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContentMarkdown(),
                document.getSlug(),
                document.getPath(),
                document.getDepartment().getId(),
                document.getParent() != null ? document.getParent().getId() : null,
                document.getPosition(),
                document.getStatus().toString(),
                document.getCreatedBy().getName(),
                document.getPublishedAt());
    }


}
