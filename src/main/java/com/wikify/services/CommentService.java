package com.wikify.services;

import com.wikify.dto.CommentDTO;
import com.wikify.dto.CommentResponse;
import com.wikify.entity.Comment;
import com.wikify.entity.Document;
import com.wikify.entity.User;
import com.wikify.repositories.CommentRepository;
import com.wikify.repositories.DocumentRepository;
import com.wikify.security.DepartmentSecurity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DepartmentSecurity departmentSecurity;
    private final DocumentRepository documentRepository;


    @Transactional(readOnly = true)
    public List<CommentResponse> getAllComments(Long documentId, User user) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if(!departmentSecurity.canRead(user, document.getDepartment().getId())) {
            throw new AccessDeniedException("Sem acesso ao comentarios");
        }

        return commentRepository.findAllByDocumentId(documentId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public void createComment(CommentDTO commentDTO, User user, Long documentId) {

        Document document = documentRepository.findById(documentId).orElseThrow(
                () -> new EntityNotFoundException("Comentario não encontrado")
        );

        if(!departmentSecurity.canRead(user, document.getDepartment().getId())) {
            throw new AccessDeniedException("Sem acesso ao comentarios");
        }

        commentRepository.save(Comment.builder().author(user)
                .document(document)
                .content(commentDTO.content())
                .build());
    }

    @Transactional
    public void deleteComment(Long commentId, User user, Long documentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new EntityNotFoundException("Comentario não encontrado")
        );;
        Document document = documentRepository.findById(documentId).orElseThrow(
                () -> new EntityNotFoundException("Documento não encontrado")
        );;

        if(!comment.getAuthor().getId().equals(user.getId()) && !departmentSecurity.isManager(user, document.getDepartment().getId())) {
            throw new AccessDeniedException("O comentario não pode ser deletado");
        }

        commentRepository.deleteByIdAndDocumentId(commentId, documentId);
    }


}
