package com.smarthas.api.repository;

import com.smarthas.api.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Alert> findByStatusOrderByCreatedAtDesc(String status);

    List<Alert> findTop100ByOrderByCreatedAtDesc();

    Optional<Alert> findFirstByMeasurementIdOrderByIdDesc(Long measurementId);

    Optional<Alert> findByMeasurementIdAndType(Long measurementId, String type);

    long countByUserIdAndStatus(Long userId, String status);

    long countByUserIdAndStatusAndSeverity(Long userId, String status, String severity);

    @Modifying
    @Query("delete from Alert a where a.measurementId = :measurementId")
    void deleteByMeasurementId(@Param("measurementId") Long measurementId);
}
