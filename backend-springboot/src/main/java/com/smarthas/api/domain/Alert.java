package com.smarthas.api.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Alerta clinico. Tabela Oracle: T_SHAS_ALERTA.
 * No perfil "oracle" os registros sao criados pela procedure PRC_SHAS_REGISTRAR_ALERTA;
 * a API apenas le e atualiza o status (resolver).
 */
@Entity
@Table(name = "T_SHAS_ALERTA",
       uniqueConstraints = @UniqueConstraint(name = "UK_SHAS_ALERTA_MED_TIPO",
                                             columnNames = {"ID_MEDICAO", "TP_ALERTA"}))
public class Alert {

    public static final String STATUS_OPEN = "ABERTO";
    public static final String STATUS_RESOLVED = "RESOLVIDO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ALERTA")
    private Long id;

    @Column(name = "ID_USUARIO", nullable = false)
    private Long userId;

    @Column(name = "ID_MEDICAO", nullable = false)
    private Long measurementId;

    @Column(name = "TP_ALERTA", nullable = false, length = 30)
    private String type;            // CRISE_HIPERTENSIVA, HIPERTENSAO, TENDENCIA_ALTA

    @Column(name = "NV_SEVERIDADE", nullable = false, length = 10)
    private String severity;        // MODERADO, ALTO, CRITICO

    @Column(name = "DS_MENSAGEM", nullable = false, length = 400)
    private String message;

    @Column(name = "ST_ALERTA", nullable = false, length = 10)
    private String status = STATUS_OPEN;

    @Column(name = "DT_ALERTA", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "DT_RESOLUCAO")
    private Instant resolvedAt;

    public Alert() { }

    public Alert(Long userId, Long measurementId, String type, String severity, String message) {
        this.userId = userId;
        this.measurementId = measurementId;
        this.type = type;
        this.severity = severity;
        this.message = message;
    }

    public void resolve() {
        this.status = STATUS_RESOLVED;
        this.resolvedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getMeasurementId() { return measurementId; }
    public String getType() { return type; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
}
