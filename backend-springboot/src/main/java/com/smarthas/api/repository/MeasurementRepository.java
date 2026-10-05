package com.smarthas.api.repository;

import com.smarthas.api.domain.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    /** Historico do usuario, da leitura mais recente para a mais antiga. */
    List<Measurement> findByUserIdOrderByMeasuredAtDescIdDesc(Long userId);

    /** Leituras do usuario a partir de uma data (indicadores por periodo). */
    List<Measurement> findByUserIdAndMeasuredAtGreaterThanEqual(Long userId, LocalDateTime from);

    /** As 5 leituras ate um instante (regra de tendencia do alerta). */
    List<Measurement> findTop5ByUserIdAndMeasuredAtLessThanEqualOrderByMeasuredAtDesc(Long userId, LocalDateTime until);
}
