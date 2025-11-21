package com.pharmaease.repository;

import com.pharmaease.model.Pharmacist;
import com.pharmaease.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportType(Report.ReportType reportType);
    List<Report> findByGeneratedBy(Pharmacist pharmacist);
    List<Report> findByStartDateBetween(LocalDate start, LocalDate end);
    List<Report> findByReportTypeAndStartDateBetween(Report.ReportType reportType, LocalDate start, LocalDate end);
}