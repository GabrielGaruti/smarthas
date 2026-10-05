package com.smarthas.api.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Medicao de pressao arterial. Tabela Oracle: T_SHAS_MEDICAO.
 *
 * Fase 6: data e hora deixaram de ser duas Strings e passaram a ser um unico
 * LocalDateTime (coluna DT_MEDICAO TIMESTAMP), o que permite ordenar, filtrar por
 * periodo e calcular indicadores no banco. O contrato JSON (date/time) foi mantido.
 */
@Entity
@Table(name = "T_SHAS_MEDICAO")
public class Measurement {

    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_SENSOR = "SENSOR";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private User user;

    @Column(name = "ID_DISPOSITIVO")
    private Long deviceId;          // preenchido quando a leitura vem de um sensor IoT

    @Column(name = "VL_SISTOLICA", nullable = false)
    private int systolic;           // mmHg

    @Column(name = "VL_DIASTOLICA", nullable = false)
    private int diastolic;          // mmHg

    @Column(name = "VL_FREQ_CARDIACA")
    private Integer heartRate;      // bpm (opcional)

    @Column(name = "DT_MEDICAO", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "TP_ORIGEM", nullable = false, length = 10)
    private String source = SOURCE_MANUAL;

    @Column(name = "DS_OBSERVACAO", length = 500)
    private String notes;

    @Column(name = "DT_REGISTRO", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Measurement() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public int getSystolic() { return systolic; }
    public void setSystolic(int systolic) { this.systolic = systolic; }

    public int getDiastolic() { return diastolic; }
    public void setDiastolic(int diastolic) { this.diastolic = diastolic; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public LocalDateTime getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(LocalDateTime measuredAt) { this.measuredAt = measuredAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    /** Data no formato ISO (yyyy-MM-dd) - mantem o contrato JSON das fases anteriores. */
    public String getDate() {
        return measuredAt == null ? null : measuredAt.format(DATE_FMT);
    }

    /** Hora no formato HH:mm - mantem o contrato JSON das fases anteriores. */
    public String getTime() {
        return measuredAt == null ? null : measuredAt.format(TIME_FMT);
    }

    /** Classificacao calculada (nao persistida) a partir dos valores. */
    @Transient
    public Classification getClassification() {
        return Classification.of(systolic, diastolic);
    }
}
