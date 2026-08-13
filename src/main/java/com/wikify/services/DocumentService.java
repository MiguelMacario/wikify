package com.wikify.services;

import com.wikify.entity.Department;
import com.wikify.entity.Document;
import com.wikify.entity.Revision;
import com.wikify.entity.User;
import com.wikify.repositories.DepartmentRepository;
import com.wikify.repositories.DocumentRepository;
import com.wikify.repositories.RevisionRepository;
import com.wikify.security.DepartmentSecurity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;


@Service
public class DocumentService{

    private final DocumentRepository documentRepository;
    private final DepartmentRepository departmentRepository;
    private final RevisionRepository revisionRepository;
    private final DepartmentSecurity departmentSecurity;

    public DocumentService(DocumentRepository documentRepository, DepartmentRepository departmentRepository, RevisionRepository revisionRepository, DepartmentSecurity departmentSecurity) {
        this.documentRepository = documentRepository;
        this.departmentRepository = departmentRepository;
        this.revisionRepository = revisionRepository;
        this.departmentSecurity = departmentSecurity;
    }

    @Transactional
    public Document saveDocument(String title, String contentMarkdown, Long departmentId,Long parentId,int position, User author){

        if (!departmentSecurity.isManager(author, departmentId)) {
            throw new AccessDeniedException("Você não gerencia esse departamento");
        }

        String pathParent;
        Department newDepartment = departmentRepository.findById(departmentId).orElseThrow();
        String slug = slugify(title);
        Document parent = null;

        if (parentId != null) {
            parent = documentRepository.findById(parentId).orElseThrow();
            if (!departmentId.equals(parent.getDepartment().getId())) {
                throw new IllegalArgumentException("O documento pai precisa ser do mesmo departamento");
            }
            pathParent = parent.getPath() + "/" + slug;
        } else {
            pathParent = newDepartment.getSlug() + "/" + slug;
        }

        if (documentRepository.existsByPath(pathParent)) {
            throw new IllegalArgumentException("Já existe um documento em " + pathParent);
        }

        Document newDocument = documentRepository.save(new Document(title, contentMarkdown, newDepartment,slug, pathParent, position));
        newDocument.setParent(parent);
        documentRepository.save(newDocument);
        revisionRepository.save(new Revision(newDocument, author));

        return newDocument;
    }

    @Transactional
    public Document updateDocument(Long documentId, String title, String contentMarkdown, User author) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado"));

        if (!departmentSecurity.isManager(author, document.getDepartment().getId())) {
            throw new AccessDeniedException("Você não gerencia esse departamento");
        }

        document.setTitle(title);
        document.setContentMarkdown(contentMarkdown);

        revisionRepository.save(new Revision(document, author));

        return document;
    }

    private static String slugify(String title) {
        String standardize = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return standardize.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }




}
