package com.wikify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "revisions",
        indexes = @Index(name = "idx_revisions_document_id", columnList = "document_id")
)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT", nullable = false, name = "content_markdown")
    private String contentMarkdown;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Revision(Document document,  User author) {
        this.document = document;
        this.title = document.getTitle();
        this.contentMarkdown = document.getContentMarkdown();
        this.author = author;
    }


}
