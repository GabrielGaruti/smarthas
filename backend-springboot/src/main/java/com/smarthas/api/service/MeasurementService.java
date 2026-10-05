package com.smarthas.api.service;

import com.smarthas.api.domain.Measurement;
import com.smarthas.api.domain.User;
import com.smarthas.api.dto.MeasurementRequest;
import com.smarthas.api.event.MeasurementRegisteredEvent;
import com.smarthas.api.repository.AlertRepository;
import com.smarthas.api.repository.MeasurementRepository;
import com.smarthas.api.web.ApiException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * CRUD de medicoes, sempre no escopo do usuario autenticado.
 * Fase 6: operacoes transacionais + publicacao do evento que aciona a regra de alerta no banco.
 */
@Service
public class MeasurementService {

    private final MeasurementRepository repository;
    private final AlertRepository alertRepository;
    private final ApplicationEventPublisher events;

    public MeasurementService(MeasurementRepository repository,
                              AlertRepository alertRepository,
                              ApplicationEventPublisher events) {
        this.repository = repository;
        this.alertRepository = alertRepository;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Measurement> listForUser(User user) {
        return repository.findByUserIdOrderByMeasuredAtDescIdDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public Measurement getOwned(Long id, User user) {
        Measurement m = repository.findById(id)
                .orElseThrow(() -> new ApiException("Medicao nao encontrada", HttpStatus.NOT_FOUND));
        if (!m.getUser().getId().equals(user.getId())) {
            throw new ApiException("Acesso negado a esta medicao", HttpStatus.FORBIDDEN);
        }
        return m;
    }

    @Transactional
    public Measurement create(MeasurementRequest req, User user) {
        Measurement m = new Measurement();
        m.setUser(user);
        apply(m, req);
        Measurement saved = repository.save(m);
        events.publishEvent(new MeasurementRegisteredEvent(saved.getId(), user.getId()));
        return saved;
    }

    @Transactional
    public Measurement update(Long id, MeasurementRequest req, User user) {
        Measurement m = getOwned(id, user);
        apply(m, req);
        Measurement saved = repository.save(m);
        events.publishEvent(new MeasurementRegisteredEvent(saved.getId(), user.getId()));
        return saved;
    }

    @Transactional
    public void delete(Long id, User user) {
        Measurement m = getOwned(id, user);
        alertRepository.deleteByMeasurementId(m.getId());   // no Oracle a FK ja tem ON DELETE CASCADE
        repository.delete(m);
    }

    private void apply(Measurement m, MeasurementRequest req) {
        m.setSystolic(req.systolic());
        m.setDiastolic(req.diastolic());
        m.setHeartRate(req.heartRate());
        m.setMeasuredAt(parseDateTime(req.date(), req.time()));
        m.setNotes(req.notes());
        if (req.deviceId() != null) {
            m.setDeviceId(req.deviceId());
            m.setSource(Measurement.SOURCE_SENSOR);
        }
    }

    private LocalDateTime parseDateTime(String date, String time) {
        try {
            return LocalDateTime.of(LocalDate.parse(date.trim()), LocalTime.parse(time.trim()));
        } catch (DateTimeParseException ex) {
            throw new ApiException("Data/hora invalidas. Use AAAA-MM-DD e HH:MM.", HttpStatus.BAD_REQUEST);
        }
    }
}
