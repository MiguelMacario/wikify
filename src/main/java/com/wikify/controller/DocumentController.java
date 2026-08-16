package com.wikify.controller;

import com.wikify.dto.*;
import com.wikify.entity.User;
import com.wikify.services.DocumentService;
import com.wikify.services.RevisionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/docs")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final RevisionService revisionService;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@RequestBody CreateDocumentRequest createDocumentRequest, @AuthenticationPrincipal User user) {
        try {
            DocumentResponse created = documentService.saveDocument(createDocumentRequest, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishDocument(@PathVariable Long id, @AuthenticationPrincipal User user){
        try {
            documentService.publishDocument(id, user);
            return ResponseEntity.ok().build();
        } catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> UpdateDocument(@PathVariable Long id, @RequestBody UpdateDocumentRequest updateDocumentRequest, @AuthenticationPrincipal User user) {
        try {
            documentService.updateDocument(id, updateDocumentRequest.title(),updateDocumentRequest.contentMarkdown(),user);
            return ResponseEntity.ok().build();
        } catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping(value = "/{id}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> importMarkdown(@PathVariable Long id, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {
        try {
            documentService.importMarkdown(id, file, user);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{*path}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable String path, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(documentService.getDocument(path, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tree")
    public ResponseEntity<List<DocumentTreeNode>> getTree(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(documentService.getTree(user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/search")
    public ResponseEntity<List<DocumentSearchProjection>> search(@RequestParam("q") String q, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(documentService.search(q, user));
    }

    @GetMapping("/{id}/edit")
    public ResponseEntity<DocumentResponse> getDraft(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(documentService.getForEdit(id, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/edit/{departmentId}")
    public ResponseEntity<List<DocumentResponse>> getDraftDocuments(@PathVariable Long departmentId ,@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(documentService.getDrafts(departmentId, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<List<DocumentResponse>> getDepartmentDocuments(@PathVariable Long departmentId ,@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(documentService.getDepartmentDocuments(departmentId, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishDocument(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            documentService.unpublishDocument(id, user);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }



    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<RevisionDTO>> getRevisions(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(revisionService.getRevisions(id, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/revisions/{revId}")
    public ResponseEntity<RevisionResponse> getRevision(@PathVariable Long id, @PathVariable Long revId, @AuthenticationPrincipal User user){
        try {
            return ResponseEntity.ok(revisionService.getRevision(id, user, revId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/revisions/{revId}/restore")
    public ResponseEntity<RevisionResponse> restoreRevision(@PathVariable Long id, @PathVariable Long revId, @AuthenticationPrincipal User user){
        try {
            return ResponseEntity.ok(revisionService.restore(id, user, revId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
