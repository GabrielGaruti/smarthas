package com.smarthas.api.integration;

import java.time.LocalDateTime;

/** Registro de T_SHAS_AUDITORIA_ALERTA, gravado pela trigger TRG_SHAS_ALERTA_AUDITORIA. */
public record AlertAuditEntry(Long id, Long alertId, String operation, String previousStatus, String newStatus,
                              String severity, String dbUser, String origin, LocalDateTime at) { }
