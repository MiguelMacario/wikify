package com.wikify.controller;


import com.wikify.dto.CommentDTO;
import com.wikify.dto.CommentResponse;
import com.wikify.entity.User;
import com.wikify.services.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/docs")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getAllComments(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(commentService.getAllComments(id, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDTO> createComment(@PathVariable Long id, @AuthenticationPrincipal User user, @RequestBody CommentDTO commentDTO) {
        try {
            commentService.createComment(commentDTO, user, id);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/comments/{cid}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @PathVariable Long cid, @AuthenticationPrincipal User user) {
        try {
            commentService.deleteComment(cid, user, id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
