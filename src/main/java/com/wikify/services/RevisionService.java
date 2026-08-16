package com.wikify.services;

import com.wikify.dto.RevisionDTO;
import com.wikify.dto.RevisionResponse;
import com.wikify.entity.Document;
import com.wikify.entity.Revision;
import com.wikify.entity.User;
import com.wikify.repositories.DocumentRepository;
import com.wikify.repositories.RevisionRepository;
import com.wikify.security.DepartmentSecurity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RevisionService {

    private final RevisionRepository revisionRepository;
    private final DepartmentSecurity departmentSecurity;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<RevisionDTO> getRevisions(Long documentId, User user) {

        requireReadableDocument(documentId, user);

        return revisionRepository.findByDocumentIdWithAuthor(documentId).stream()
                .map(RevisionDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RevisionResponse getRevision(Long documentId, User user, Long revisionId) {

        requireReadableDocument(documentId, user);

        Revision revision = revisionRepository.findByIdAndDocumentId(revisionId, documentId)
                .orElseThrow(() -> new EntityNotFoundException("Revisão não encontrada"));

        return RevisionResponse.from(revision);
    }

    @Transactional
    public RevisionResponse restore(Long documentId, User user, Long revisionId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if (!departmentSecurity.canEdit(user, document)) {
            throw new AccessDeniedException("Você não pode editar este documento");
        }

        Revision revision = revisionRepository.findByIdAndDocumentId(revisionId, documentId)
                .orElseThrow(() -> new EntityNotFoundException("Revisão não encontrada"));

        document.setTitle(revision.getTitle());
        document.setContentMarkdown(revision.getContentMarkdown());

        Revision nova = revisionRepository.save(Revision.builder().document(document).author(user).build());

        return RevisionResponse.from(nova);
    }

    private Document requireReadableDocument(Long documentId, User user) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if (!departmentSecurity.canRead(user, document.getDepartment().getId())) {
            throw new AccessDeniedException("Você não tem acesso a este documento");
        }

        return document;
    }
}
