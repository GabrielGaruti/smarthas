package com.smarthas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nome e obrigatorio") String fullName,
        @NotBlank(message = "E-mail e obrigatorio") @Email(message = "E-mail invalido") String email,
        @NotBlank(message = "Senha e obrigatoria") @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres") String password
) { }
