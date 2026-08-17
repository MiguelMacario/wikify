package com.wikify.dto;

import com.wikify.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(Long id,Long authorId, String authorName,Long documentId, String content, LocalDateTime createdAt) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getDocument().getId(),
                comment.getContent(),
                comment.getCreatedAt());
    }
}
