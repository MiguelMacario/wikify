package com.wikify.repositories;

import com.wikify.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    void deleteByIdAndDocumentId(Long commentId, Long documentId);
    List<Comment> findAllByDocumentId(Long documentId);

}
