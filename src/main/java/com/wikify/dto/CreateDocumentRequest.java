package com.wikify.dto;

public record CreateDocumentRequest(Long departmentId, Long parentId, String title, String contentMarkdown, int position) {
}
