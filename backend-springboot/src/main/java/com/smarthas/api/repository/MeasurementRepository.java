package com.smarthas.api.repository;

import com.smarthas.api.domain.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByUserIdOrderByIdDesc(Long userId);
}
