package com.smarthas.api.service;

import com.smarthas.api.domain.HealthUnit;
import com.smarthas.api.dto.HealthUnitRequest;
import com.smarthas.api.repository.HealthUnitRepository;
import com.smarthas.api.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/** CRUD das unidades de saude / sensores (pontos do mapa). */
@Service
public class HealthUnitService {

    private final HealthUnitRepository repository;

    public HealthUnitService(HealthUnitRepository repository) {
        this.repository = repository;
    }

    public List<HealthUnit> list() {
        return repository.findAll();
    }

    public HealthUnit get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException("Unidade nao encontrada", HttpStatus.NOT_FOUND));
    }

    public HealthUnit create(HealthUnitRequest req) {
        HealthUnit u = new HealthUnit();
        apply(u, req);
        return repository.save(u);
    }

    public HealthUnit update(Long id, HealthUnitRequest req) {
        HealthUnit u = get(id);
        apply(u, req);
        return repository.save(u);
    }

    public void delete(Long id) {
        HealthUnit u = get(id);
        repository.delete(u);
    }

    private void apply(HealthUnit u, HealthUnitRequest req) {
        u.setName(req.name());
        u.setType(req.type());
        u.setLatitude(req.latitude());
        u.setLongitude(req.longitude());
        u.setAddress(req.address());
        u.setActive(req.active() == null || req.active());
    }
}
