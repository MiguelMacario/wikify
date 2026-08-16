package com.wikify.services;

import com.wikify.dto.CreateDocumentRequest;
import com.wikify.dto.DocumentResponse;
import com.wikify.dto.DocumentSearchProjection;
import com.wikify.dto.DocumentTreeNode;
import com.wikify.entity.Department;
import com.wikify.entity.Document;
import com.wikify.entity.Revision;
import com.wikify.entity.User;
import com.wikify.entity.enums.Status;
import com.wikify.repositories.DepartmentRepository;
import com.wikify.repositories.DocumentRepository;
import com.wikify.repositories.RevisionRepository;
import com.wikify.repositories.UserRepository;
import com.wikify.security.DepartmentSecurity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class DocumentService{

    private final DocumentRepository documentRepository;
    private final DepartmentRepository departmentRepository;
    private final RevisionRepository revisionRepository;
    private final DepartmentSecurity departmentSecurity;

    @Transactional
    public DocumentResponse saveDocument(CreateDocumentRequest createDocumentRequest, User author){

        if (!departmentSecurity.canContribute(author, createDocumentRequest.departmentId())) {
            throw new AccessDeniedException("Você não contribui com esse departamento");
        }

        String pathParent;
        Department newDepartment = departmentRepository.findById(createDocumentRequest.departmentId())
                .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado"));
        String slug = slugify(createDocumentRequest.title());
        Document parent = null;

        if (createDocumentRequest.parentId() != null) {
            parent = documentRepository.findById(createDocumentRequest.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Documento pai não encontrado"));
            if (!createDocumentRequest.departmentId().equals(parent.getDepartment().getId())) {
                throw new IllegalArgumentException("O documento pai precisa ser do mesmo departamento");
            }
            pathParent = parent.getPath() + "/" + slug;
        } else {
            pathParent = newDepartment.getSlug() + "/" + slug;
        }

        if (documentRepository.existsByPath(pathParent)) {
            throw new IllegalArgumentException("Já existe um documento em " + pathParent);
        }

        rejectDangerousHtml(createDocumentRequest.contentMarkdown());

        Document newDocument = Document.builder().title(createDocumentRequest.title())
                .contentMarkdown(createDocumentRequest.contentMarkdown())
                .department(newDepartment)
                .slug(slug)
                .path(pathParent)
                .position(createDocumentRequest.position())
                .createdBy(author)
                .build();
        newDocument.setParent(parent);
        documentRepository.save(newDocument);
        revisionRepository.save(Revision.builder().document(newDocument).author(author).build());

        return DocumentResponse.from(newDocument);
    }

    @Transactional
    public void updateDocument(Long documentId, String title, String contentMarkdown, User author) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if (!departmentSecurity.canEdit(author, document)) {
            throw new AccessDeniedException("Você não pode editar este documento");
        }

        rejectDangerousHtml(contentMarkdown);

        document.setTitle(title);
        document.setContentMarkdown(contentMarkdown);

        revisionRepository.save(Revision.builder().document(document).author(author).build());
    }

    @Transactional
    public void publishDocument(Long documentId, User author) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if (!departmentSecurity.canPublish(author, document.getDepartment().getId())) {
            throw new AccessDeniedException("Você não contribui com esse departamento");
        }

        document.setStatus(Status.PUBLISHED);
        document.setPublishedAt(LocalDateTime.now());
    }

    @Transactional
    public void importMarkdown(Long documentId, MultipartFile file, User author) {

        String content = readMarkdown(file);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        updateDocument(documentId, document.getTitle(), content, author);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(String path, User user) {
        Set<Long> readableIds = departmentSecurity.readableDepartmentIds(user);
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;

        if (readableIds.isEmpty()) {
            throw new EntityNotFoundException("Documento não encontrado");
        }

        return documentRepository
                .findByPathAndStatusAndDepartmentIdIn(cleanPath, Status.PUBLISHED, readableIds)
                .map(DocumentResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

    }

    @Transactional(readOnly = true)
    public DocumentResponse getForEdit(Long id, User user) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if (!departmentSecurity.canEdit(user, document)) {
            throw new EntityNotFoundException("Documento não encontrado");
        }

        return DocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDrafts(Long departmentId, User user) {
        if (!departmentSecurity.canContribute(user, departmentId)) {
            return List.of();
        }

        return documentRepository.findByDepartmentIdAndStatus(departmentId, Status.DRAFT).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDepartmentDocuments(Long departmentId, User user) {
        if (!departmentSecurity.canContribute(user, departmentId)) {
            return List.of();
        }

        return documentRepository.findByDepartmentIdAndStatus(departmentId, Status.PUBLISHED).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional
    public void unpublishDocument(Long id, User user) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if (!departmentSecurity.isManager(user, document.getDepartment().getId())) {
            throw new AccessDeniedException("Só o gestor do departamento pode despublicar");
        }

        document.setStatus(Status.DRAFT);
    }


    @Transactional(readOnly = true)
    public List<DocumentTreeNode> getTree(User user) {

        Set<Long> readableIds = departmentSecurity.readableDepartmentIds(user);
        if (readableIds.isEmpty()) {
            return List.of();
        }

        List<Document> documents = documentRepository
                .findByDepartmentIdInAndStatusOrderByPositionAscTitleAsc(
                        readableIds, Status.PUBLISHED);

        Map<Long, DocumentTreeNode> nodesById = new LinkedHashMap<>();
        for (Document document : documents) {
            nodesById.put(document.getId(), DocumentTreeNode.from(document));
        }

        List<DocumentTreeNode> roots = new ArrayList<>();
        for (Document document : documents) {
            DocumentTreeNode node = nodesById.get(document.getId());

            Long parentId = document.getParent() != null ? document.getParent().getId() : null;
            DocumentTreeNode parentNode = parentId != null ? nodesById.get(parentId) : null;

            if (parentNode != null) {
                parentNode.children().add(node);
            } else {
                roots.add(node);
            }
        }

        return roots;
    }

    @Transactional(readOnly = true)
    public List<DocumentSearchProjection> search(String termo, User user) {

        if (termo == null || termo.isBlank() || termo.trim().length() < 3) {
            return List.of();
        }

        Set<Long> readableIds = departmentSecurity.readableDepartmentIds(user);
        if (readableIds.isEmpty()) {
            return List.of();
        }

        return documentRepository.search(termo.trim(), readableIds);
    }

    private static String slugify(String title) {
        String standardize = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return standardize.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String readMarkdown(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);

            String content = decoder.decode(ByteBuffer.wrap(file.getBytes())).toString();

            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }

            return content.replace("\r\n", "\n");

        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("O arquivo não está em UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo");
        }
    }


    private static final Pattern DANGER_HTML = Pattern.compile(
            "<\\s*script"
                    + "|<\\s*iframe"
                    + "|<\\s*object"
                    + "|<\\s*embed"
                    + "|javascript\\s*:"
                    + "|[\\s/]on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CODE_BLOCK = Pattern.compile(
            "```[\\s\\S]*?```" +
                    "|`[^`\\n]*`");

    private void rejectDangerousHtml(String content) {
        String codeBlock = CODE_BLOCK.matcher(content).replaceAll("");
        Matcher matcher = DANGER_HTML.matcher(codeBlock);

        if (matcher.find()) {
            throw new IllegalArgumentException(
                    "O conteúdo contém HTML não permitido: \"" + matcher.group().trim() + "\". "
                            + "Markdown é aceito normalmente; script, iframe e atributos de evento não.");
        }
    }


}
