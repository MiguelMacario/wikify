package com.wikify.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "document_department_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "department_id"})
)
@NoArgsConstructor
@Getter
@Setter
public class DocumentDepartmentAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public DocumentDepartmentAccess(Document document, Department department) {
        this.document = document;
        this.department = department;
    }
}
