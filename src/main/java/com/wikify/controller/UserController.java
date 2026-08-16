package com.wikify.controller;

import com.wikify.dto.UserSummaryDTO;
import com.wikify.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    @PreAuthorize("hasRole('SYSADMIN') or @departmentSecurity.isAnyManager(authentication.principal)")
    public ResponseEntity<List<UserSummaryDTO>> listUsers() {
        List<UserSummaryDTO> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }
}
