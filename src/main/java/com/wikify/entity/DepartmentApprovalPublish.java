package com.wikify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "department_approval_publish", uniqueConstraints = {
        @UniqueConstraint(name = "uk_department_approval_config", columnNames = {"department_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentApprovalPublish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false, unique = true)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Builder.Default
    @Column(name = "approval_required", nullable = false)
    private boolean approvePublish = false;

    @Column(name = "changed_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime changedAt;

}
