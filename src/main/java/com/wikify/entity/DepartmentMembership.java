package com.wikify.entity;

import com.wikify.entity.enums.DepartmentRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(
        name = "department_membership",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "department_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class DepartmentMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentRole role;

    public DepartmentMembership(User user, Department department, DepartmentRole role) {
        this.user = user;
        this.department = department;
        this.role = role;
    }
}
