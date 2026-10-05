package com.smarthas.api.service;

import com.smarthas.api.domain.Alert;
import com.smarthas.api.domain.Role;
import com.smarthas.api.domain.User;
import com.smarthas.api.repository.AlertRepository;
import com.smarthas.api.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Consulta e tratamento dos alertas gerados pela regra clinica. */
@Service
public class AlertService {

    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Alert> listForUser(User user) {
        return repository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Alert> listAll(String status) {
        return status == null || status.isBlank()
                ? repository.findTop100ByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
    }

    @Transactional(readOnly = true)
    public Optional<Alert> findForMeasurement(Long measurementId) {
        return repository.findFirstByMeasurementIdOrderByIdDesc(measurementId);
    }

    /** O paciente resolve os proprios alertas; o ADMIN resolve qualquer um. */
    @Transactional
    public Alert resolve(Long id, User user) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new ApiException("Alerta nao encontrado", HttpStatus.NOT_FOUND));
        if (user.getRole() != Role.ADMIN && !alert.getUserId().equals(user.getId())) {
            throw new ApiException("Acesso negado a este alerta", HttpStatus.FORBIDDEN);
        }
        alert.resolve();
        return repository.save(alert);
    }
}
