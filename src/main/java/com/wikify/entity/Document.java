package com.wikify.entity;


import com.wikify.entity.enums.EditPolicy;
import com.wikify.entity.enums.Status;
import com.wikify.entity.enums.Validation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false, unique = true)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Document parent;

    private int position;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Validation validation = Validation.NONE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "edit_policy", nullable = false)
    private EditPolicy editPolicy = EditPolicy.AUTHOR_ONLY;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(columnDefinition = "TEXT", nullable = false, name = "content_markdown")
    private String contentMarkdown;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "validation_at")
    private LocalDateTime validationAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_by")
    private User validationBy;

    private String rejectionReason;


    public boolean isRoot() {
        return parent == null;
    }

    public boolean isChild() {
        return parent != null;
    }

}
