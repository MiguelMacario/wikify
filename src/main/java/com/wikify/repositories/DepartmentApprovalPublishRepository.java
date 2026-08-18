package com.wikify.repositories;

import com.wikify.entity.DepartmentApprovalPublish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentApprovalPublishRepository extends JpaRepository<DepartmentApprovalPublish, Long> {
    Optional<DepartmentApprovalPublish> findByDepartmentId(Long departmentId);
}
