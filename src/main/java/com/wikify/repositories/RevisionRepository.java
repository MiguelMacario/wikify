package com.wikify.repositories;

import com.wikify.entity.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RevisionRepository extends JpaRepository<Revision, Long> {

    @Query("""
            SELECT r FROM Revision r
            JOIN FETCH r.author
            WHERE r.document.id = :documentId
            ORDER BY r.createdAt DESC
            """)
    List<Revision> findByDocumentIdWithAuthor(Long documentId);

    Optional<Revision> findByIdAndDocumentId(Long id, Long documentId);
}
