package com.smarthas.api.dto;

import com.smarthas.api.domain.User;

/** Representacao publica de um usuario (sem hash de senha). */
public record UserResponse(Long id, String email, String fullName, String role) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole().name());
    }
}
