package com.wikify.dto;

import com.wikify.entity.Document;

public record DocumentResponse(String title, String content, String slug, String path, Long departmentId,
                               Long parentId, int position, String status) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getTitle(),
                document.getContentMarkdown(),
                document.getSlug(),
                document.getPath(),
                document.getDepartment().getId(),
                document.getParent() != null ? document.getParent().getId() : null,
                document.getPosition(),
                document.getStatus().toString());
    }


}
