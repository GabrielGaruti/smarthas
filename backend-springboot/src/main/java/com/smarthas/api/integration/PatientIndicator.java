package com.smarthas.api.integration;

/** Linha do painel de indicadores por paciente. */
public record PatientIndicator(Long userId, String fullName, Double controlRate, String summary) { }
