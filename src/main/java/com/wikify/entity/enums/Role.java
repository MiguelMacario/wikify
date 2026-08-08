package com.wikify.entity.enums;
import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Admin"),
    USER("User");

    private String role;

    Role(String role) {this.role = role;}


}
