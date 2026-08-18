package com.wikify.services;

import com.wikify.dto.ApprovalToggleResponse;
import com.wikify.dto.DepartmentApproveDTO;
import com.wikify.dto.DepartmentApproveResponse;
import com.wikify.entity.Document;
import com.wikify.entity.DepartmentApprovalPublish;
import com.wikify.entity.User;
import com.wikify.entity.enums.Status;
import com.wikify.entity.enums.Validation;
import com.wikify.repositories.DepartmentApprovalPublishRepository;
import com.wikify.repositories.DocumentRepository;

import com.wikify.security.DepartmentSecurity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentApprovalPublishService {

    private final DepartmentApprovalPublishRepository departmentApprovalPublishRepository;
    private final DocumentRepository documentRepository;
    private final DepartmentSecurity departmentSecurity;

    @Transactional
    public ApprovalToggleResponse toggleApprovement(DepartmentApproveDTO departmentApproveDTO, User user) {
        Long departmentId = departmentApproveDTO.departmentId();

        if (!departmentSecurity.isManager(user, departmentId)) {
            throw new AccessDeniedException("Acesso negado");
        }

        DepartmentApprovalPublish config = departmentApprovalPublishRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Opção não encontrada"));

        boolean isOn = config.isApprovePublish();
        boolean willOn = departmentApproveDTO.approvePublish();

        config.setApprovePublish(willOn);
        config.setChangedBy(user);

        int promoted = (isOn && !willOn) ? releaseLocked(departmentId) : 0;

        return new ApprovalToggleResponse(willOn, promoted);
    }

    private int releaseLocked(Long departmentId) {
        List<Document> lockeds = documentRepository
                .findByDepartmentIdAndStatusAndValidation(departmentId, Status.DRAFT, Validation.PENDING);

        LocalDateTime agora = LocalDateTime.now();
        for (Document document : lockeds) {
            document.setStatus(Status.PUBLISHED);
            document.setPublishedAt(agora);
        }

        return lockeds.size();
    }

    @Transactional(readOnly = true)
    public DepartmentApproveResponse getToggledApproval(Long departmentId, User user) {
        if (!departmentSecurity.isManager(user, departmentId)) {
            throw new AccessDeniedException("Acesso negado");
        }

        return departmentApprovalPublishRepository.findByDepartmentId(departmentId)
                .map(DepartmentApproveResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Opção não encontrada"));
    }
}
