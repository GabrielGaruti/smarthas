package com.smarthas.api.domain;

/** Perfis de acesso usados na autorizacao. */
public enum Role {
    USER,   // paciente: gerencia as proprias medicoes
    ADMIN   // administrador: gerencia unidades de saude e ve todos os dados
}
