package com.smarthas.api.repository;

import com.smarthas.api.domain.SummaryReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SummaryReportRepository extends JpaRepository<SummaryReport, Long> {
    List<SummaryReport> findAllByOrderByIdDesc(Pageable pageable);
}
