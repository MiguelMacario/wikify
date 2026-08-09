package com.wikify.repositories;

import com.wikify.entity.DepartmentMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentMembershipRepository extends JpaRepository<DepartmentMembership, Long> {
    Optional<DepartmentMembership> findByUserIdAndDepartmentId(Long userId, Long departmentId);
    List<DepartmentMembership> findByUserId(Long userId);
    List<DepartmentMembership> findByDepartmentId(Long departmentId);
}
