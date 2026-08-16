package com.wikify.repositories;

import com.wikify.entity.DepartmentMembership;
import com.wikify.entity.enums.DepartmentRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentMembershipRepository extends JpaRepository<DepartmentMembership, Long> {
    Optional<DepartmentMembership> findByUserIdAndDepartmentId(Long userId, Long departmentId);
    List<DepartmentMembership> findByUserId(Long userId);
    List<DepartmentMembership> findByDepartmentId(Long departmentId);

    @Query("""
            SELECT m FROM DepartmentMembership m
            JOIN FETCH m.user u
            WHERE m.department.id = :departmentId
            ORDER BY u.name
            """)
    List<DepartmentMembership> findByDepartmentIdWithUser(Long departmentId);

    boolean existsByDepartmentIdAndUserId(Long departmentId, Long userId);

    long countByDepartmentIdAndRole(Long departmentId, DepartmentRole role);
}
