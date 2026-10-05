package com.smarthas.api.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Relatorio resumido por paciente. Tabela Oracle: T_SHAS_RELATORIO_RESUMO.
 * No perfil "oracle" e gerado pela procedure PRC_SHAS_GERAR_RELATORIO_RESUMO.
 */
@Entity
@Table(name = "T_SHAS_RELATORIO_RESUMO")
public class SummaryReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RELATORIO")
    private Long id;

    @Column(name = "ID_USUARIO", nullable = false)
    private Long userId;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDate startDate;

    @Column(name = "DT_FIM", nullable = false)
    private LocalDate endDate;

    @Column(name = "QT_MEDICOES", nullable = false)
    private int measurementCount;

    @Column(name = "VL_MEDIA_SISTOLICA")
    private Double avgSystolic;

    @Column(name = "VL_MEDIA_DIASTOLICA")
    private Double avgDiastolic;

    @Column(name = "VL_MAX_SISTOLICA")
    private Integer maxSystolic;

    @Column(name = "PC_CONTROLE")
    private Double controlRate;

    @Column(name = "QT_ALERTAS_ABERTOS", nullable = false)
    private int openAlerts;

    @Column(name = "NV_RISCO", nullable = false, length = 10)
    private String riskLevel;

    @Column(name = "DS_RESUMO", length = 400)
    private String summary;

    @Column(name = "DT_GERACAO", nullable = false, updatable = false)
    private Instant generatedAt = Instant.now();

    public SummaryReport() { }

    public SummaryReport(Long userId, LocalDate startDate, LocalDate endDate, int measurementCount,
                         Double avgSystolic, Double avgDiastolic, Integer maxSystolic, Double controlRate,
                         int openAlerts, String riskLevel, String summary) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.measurementCount = measurementCount;
        this.avgSystolic = avgSystolic;
        this.avgDiastolic = avgDiastolic;
        this.maxSystolic = maxSystolic;
        this.controlRate = controlRate;
        this.openAlerts = openAlerts;
        this.riskLevel = riskLevel;
        this.summary = summary;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getMeasurementCount() { return measurementCount; }
    public Double getAvgSystolic() { return avgSystolic; }
    public Double getAvgDiastolic() { return avgDiastolic; }
    public Integer getMaxSystolic() { return maxSystolic; }
    public Double getControlRate() { return controlRate; }
    public int getOpenAlerts() { return openAlerts; }
    public String getRiskLevel() { return riskLevel; }
    public String getSummary() { return summary; }
    public Instant getGeneratedAt() { return generatedAt; }
}
