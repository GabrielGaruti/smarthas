package com.smarthas.api.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Usuario do sistema (paciente ou administrador). Tabela Oracle: T_SHAS_USUARIO. */
@Entity
@Table(name = "T_SHAS_USUARIO")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long id;

    @Column(name = "NM_COMPLETO", nullable = false, length = 120)
    private String fullName;

    @Column(name = "DS_EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "DS_SENHA_HASH", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_PERFIL", nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(name = "DT_CADASTRO", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public User() { }

    public User(String fullName, String email, String passwordHash, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
