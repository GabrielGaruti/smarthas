package com.smarthas.api.service;

import com.smarthas.api.domain.Measurement;
import com.smarthas.api.domain.User;
import com.smarthas.api.dto.MeasurementRequest;
import com.smarthas.api.repository.MeasurementRepository;
import com.smarthas.api.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/** CRUD de medicoes, sempre no escopo do usuario autenticado. */
@Service
public class MeasurementService {

    private final MeasurementRepository repository;

    public MeasurementService(MeasurementRepository repository) {
        this.repository = repository;
    }

    public List<Measurement> listForUser(User user) {
        return repository.findByUserIdOrderByIdDesc(user.getId());
    }

    public Measurement getOwned(Long id, User user) {
        Measurement m = repository.findById(id)
                .orElseThrow(() -> new ApiException("Medicao nao encontrada", HttpStatus.NOT_FOUND));
        if (!m.getUser().getId().equals(user.getId())) {
            throw new ApiException("Acesso negado a esta medicao", HttpStatus.FORBIDDEN);
        }
        return m;
    }

    public Measurement create(MeasurementRequest req, User user) {
        Measurement m = new Measurement();
        m.setUser(user);
        apply(m, req);
        return repository.save(m);
    }

    public Measurement update(Long id, MeasurementRequest req, User user) {
        Measurement m = getOwned(id, user);
        apply(m, req);
        return repository.save(m);
    }

    public void delete(Long id, User user) {
        Measurement m = getOwned(id, user);
        repository.delete(m);
    }

    private void apply(Measurement m, MeasurementRequest req) {
        m.setSystolic(req.systolic());
        m.setDiastolic(req.diastolic());
        m.setDate(req.date());
        m.setTime(req.time());
        m.setNotes(req.notes());
    }
}
