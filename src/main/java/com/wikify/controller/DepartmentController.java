package com.wikify.controller;

import com.wikify.dto.CreateDepartmentRequest;
import com.wikify.dto.DepartmentDTO;
import com.wikify.dto.MemberDTO;
import com.wikify.dto.MemberResponse;
import com.wikify.services.DepartmentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<DepartmentDTO>> getDepartments() {
        List<DepartmentDTO> departments = departmentService.getDepartments();
        return ResponseEntity.ok(departments);
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> createDepartment(@RequestBody CreateDepartmentRequest createDepartmentRequest) {
        try {
            departmentService.createDepartment(createDepartmentRequest.managerId(),createDepartmentRequest.name(), createDepartmentRequest.slug());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('SYSADMIN') or @departmentSecurity.isManager(authentication.principal, #id)")
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getMembers(id));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('SYSADMIN') or @departmentSecurity.isManager(authentication.principal, #id)")
    public ResponseEntity<Void> addMembers(@RequestBody MemberDTO memberDTO, @PathVariable Long id) {
        try{
            departmentService.addMember(id, memberDTO.userId(), memberDTO.role());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch(IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PatchMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('SYSADMIN') or @departmentSecurity.isManager(authentication.principal, #id)")
    public ResponseEntity<Void> updateMembers(@RequestBody MemberDTO memberDTO, @PathVariable Long id, @PathVariable Long userId) {
        try{
            departmentService.changeMemberRole(id, userId, memberDTO.role());
            return ResponseEntity.ok().build();
        } catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch(IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('SYSADMIN') or @departmentSecurity.isManager(authentication.principal, #id)")
    public ResponseEntity<Void> deleteMembers(@PathVariable Long id, @PathVariable Long userId) {
        try{
            departmentService.removeMember(id, userId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch(EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch(IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }



}
