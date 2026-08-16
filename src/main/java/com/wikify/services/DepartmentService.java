package com.wikify.services;

import com.wikify.dto.DepartmentDTO;
import com.wikify.dto.MemberResponse;
import com.wikify.entity.Department;
import com.wikify.entity.DepartmentMembership;
import com.wikify.entity.User;
import com.wikify.entity.enums.DepartmentRole;
import com.wikify.repositories.DepartmentMembershipRepository;
import com.wikify.repositories.DepartmentRepository;
import com.wikify.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMembershipRepository departmentMembershipRepository;
    private final UserRepository userRepository;

    public List<DepartmentDTO> getDepartments() {
        List<DepartmentDTO> departments = new ArrayList<>();
        departmentRepository.findAll().forEach(department -> {departments.add(new DepartmentDTO(department.getId() ,department.getName(), department.getSlug()));});
        return departments;
    }


    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long departmentId) {
        return departmentMembershipRepository.findByDepartmentIdWithUser(departmentId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public void createDepartment(Long userId,String name, String slug) {
        String slugified = slugify(slug);

        if (departmentRepository.existsBySlug(slugified)) {
            throw new IllegalArgumentException("Esse dapartamento já existe");
        }

        Department savedDepartment = departmentRepository.save(Department.builder().name(name).slug(slugified).build());
        addMember(savedDepartment.getId(), userId, DepartmentRole.MANAGER);
    }

    @Transactional
    public void addMember(Long departmentId, Long userId, DepartmentRole role) {

        if (departmentMembershipRepository.existsByDepartmentIdAndUserId(departmentId, userId)) {
            throw new IllegalStateException("O usuário já faz parte deste departamento.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado " + userId));
        Department department = departmentRepository.findById(departmentId).orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado " + departmentId));

        departmentMembershipRepository.save(DepartmentMembership.builder().user(user).department(department).role(role).build());
    }

    @Transactional
    public void changeMemberRole(Long departmentId, Long userId, DepartmentRole newRole) {

        DepartmentMembership membership = departmentMembershipRepository
                .findByUserIdAndDepartmentId(userId, departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não é membro deste departamento"));

        if (membership.getRole() == DepartmentRole.MANAGER && newRole != DepartmentRole.MANAGER) {
            ensureNotLastManager(departmentId);
        }

        membership.setRole(newRole);
    }

    @Transactional
    public void removeMember(Long departmentId, Long userId) {

        DepartmentMembership membership = departmentMembershipRepository
                .findByUserIdAndDepartmentId(userId, departmentId)
                .orElseThrow(() -> new EntityNotFoundException("O usuário não é membro deste departamento"));

        if (membership.getRole() == DepartmentRole.MANAGER) {
            ensureNotLastManager(departmentId);
        }

        departmentMembershipRepository.delete(membership);
    }

    private void ensureNotLastManager(Long departmentId) {
        long managers = departmentMembershipRepository
                .countByDepartmentIdAndRole(departmentId, DepartmentRole.MANAGER);

        if (managers <= 1) {
            throw new IllegalStateException(
                    "O departamento precisa de pelo menos um gestor. Nomeie outro antes de fazer essa alteração.");
        }
    }


    private static String slugify(String title) {
        String standardize = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return standardize.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

}
