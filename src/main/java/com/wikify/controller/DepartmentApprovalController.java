package com.wikify.controller;


import com.wikify.dto.ApprovalToggleResponse;
import com.wikify.dto.DepartmentApproveDTO;
import com.wikify.dto.DepartmentApproveResponse;
import com.wikify.entity.User;
import com.wikify.services.DepartmentApprovalPublishService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approvement")
@RequiredArgsConstructor
public class DepartmentApprovalController {

    private final DepartmentApprovalPublishService departmentApprovalPublishService;


    @PatchMapping
    public ResponseEntity<ApprovalToggleResponse> toggleApprovement(@RequestBody DepartmentApproveDTO departmentApproveDTO, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(departmentApprovalPublishService.toggleApprovement(departmentApproveDTO, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentApproveResponse> getToggledApproval(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(departmentApprovalPublishService.getToggledApproval(id, user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
