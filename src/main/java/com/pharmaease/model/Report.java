package com.pharmaease.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportType reportType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column
    private Integer totalOrders = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private Pharmacist generatedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime generatedAt;

    public enum ReportType {
        DAILY_SALES, WEEKLY_SALES, MONTHLY_SALES,
        INVENTORY, LOW_STOCK, EXPIRING_STOCK,
        CUSTOMER_SALES, SUPPLIER_PURCHASES
    }
}