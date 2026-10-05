package com.smarthas.api.domain;

import jakarta.persistence.*;

/**
 * Ponto de interesse no mapa (camada "AI Logistics Extension"):
 * hospital, sensor IoT ou unidade de atendimento. Tabela Oracle: T_SHAS_UNIDADE_SAUDE.
 */
@Entity
@Table(name = "T_SHAS_UNIDADE_SAUDE")
public class HealthUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_UNIDADE")
    private Long id;

    @Column(name = "NM_UNIDADE", nullable = false, length = 120)
    private String name;

    @Column(name = "TP_UNIDADE", nullable = false, length = 10)
    private String type;    // HOSPITAL, SENSOR, CLINIC

    @Column(name = "VL_LATITUDE", nullable = false)
    private double latitude;

    @Column(name = "VL_LONGITUDE", nullable = false)
    private double longitude;

    @Column(name = "DS_ENDERECO", length = 200)
    private String address;

    @Convert(converter = BooleanToNumberConverter.class)
    @Column(name = "ST_ATIVO", nullable = false)
    private boolean active = true;

    public HealthUnit() { }

    public HealthUnit(String name, String type, double latitude, double longitude, String address) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
