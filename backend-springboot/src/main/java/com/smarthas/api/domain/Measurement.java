package com.smarthas.api.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Medicao de pressao arterial pertencente a um usuario. */
@Entity
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int systolic;   // pressao sistolica (mmHg)

    @Column(nullable = false)
    private int diastolic;  // pressao diastolica (mmHg)

    @Column(nullable = false)
    private String date;    // formato ISO yyyy-MM-dd

    @Column(nullable = false)
    private String time;    // formato HH:mm

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Measurement() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getSystolic() { return systolic; }
    public void setSystolic(int systolic) { this.systolic = systolic; }

    public int getDiastolic() { return diastolic; }
    public void setDiastolic(int diastolic) { this.diastolic = diastolic; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    /** Classificacao calculada (nao persistida) a partir dos valores. */
    @Transient
    public Classification getClassification() {
        return Classification.of(systolic, diastolic);
    }
}
