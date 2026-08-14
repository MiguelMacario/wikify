package com.wikify.security;

import com.wikify.entity.DepartmentMembership;
import com.wikify.entity.Document;
import com.wikify.entity.User;
import com.wikify.entity.enums.DepartmentRole;
import com.wikify.entity.enums.EditPolicy;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component("departmentSecurity")
public class DepartmentSecurity {

    public Set<Long> readableDepartmentIds(User user) {
        return idsWhere(user, role -> true);
    }

    public Set<Long> managedDepartmentIds(User user) {
        return idsWhere(user, role -> role == DepartmentRole.MANAGER);
    }

    public boolean canRead(User user, Long departmentId) {
        return roleIn(user, departmentId).isPresent();
    }

    public boolean canContribute(User user, Long departmentId) {
        return roleIn(user, departmentId)
                .filter(role -> role == DepartmentRole.MANAGER || role == DepartmentRole.MEMBER)
                .isPresent();
    }

    public boolean isManager(User user, Long departmentId) {
        return roleIn(user, departmentId)
                .filter(role -> role == DepartmentRole.MANAGER)
                .isPresent();
    }

    public boolean canPublish(User user, Long departmentId) {
        return canContribute(user, departmentId);
    }

    public boolean canEdit(User user, Document document) {
        if (user == null || document == null) {
            return false;
        }

        Long departmentId = document.getDepartment().getId();

        if (isManager(user, departmentId)) {
            return true;
        }
        if (!canContribute(user, departmentId)) {
            return false;
        }
        if (user.getId().equals(document.getCreatedBy().getId())) {
            return true;
        }

        return document.getEditPolicy() == EditPolicy.DEPARTMENT;
    }

    public Optional<DepartmentRole> roleIn(User user, Long departmentId) {
        if (user == null || departmentId == null) {
            return Optional.empty();
        }
        return user.getMemberships().stream()
                .filter(m -> departmentId.equals(m.getDepartment().getId()))
                .map(DepartmentMembership::getRole)
                .findFirst();
    }

    private Set<Long> idsWhere(User user, Predicate<DepartmentRole> filter) {
        if (user == null) {
            return Set.of();
        }
        return user.getMemberships().stream()
                .filter(m -> filter.test(m.getRole()))
                .map(m -> m.getDepartment().getId())
                .collect(Collectors.toUnmodifiableSet());
    }
}
