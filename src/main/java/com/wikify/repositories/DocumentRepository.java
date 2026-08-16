package com.wikify.repositories;

import com.wikify.dto.DocumentSearchProjection;
import com.wikify.entity.Document;
import com.wikify.entity.enums.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = "createdBy")
    List<Document> findByDepartmentIdAndStatus(Long departmentId, Status status);

    Optional<Document> findByIdAndStatus(Long id, Status status);

    @EntityGraph(attributePaths = "createdBy")
    Optional<Document> findByPathAndStatusAndDepartmentIdIn(String path, Status status, Set<Long> readableIds);

    List<Document> findByDepartmentIdInAndStatusOrderByPositionAscTitleAsc(Set<Long> readableIds, Status status);

    @Query(value = """
        SELECT d.id    AS id,
               d.title AS title,
               d.path  AS path,
               ts_headline('portuguese', d.content_markdown,
                           plainto_tsquery('portuguese', :termo),
                           'MaxWords=30, MinWords=15, MaxFragments=1') AS snippet
        FROM documents d
        WHERE d.search_vector @@ plainto_tsquery('portuguese', :termo)
          AND d.status = 'PUBLISHED'
          AND d.department_id IN (:departmentIds)
        ORDER BY ts_rank(d.search_vector, plainto_tsquery('portuguese', :termo)) DESC
        LIMIT 20
        """, nativeQuery = true)
    List<DocumentSearchProjection> search(String termo, Collection<Long> departmentIds);


}
