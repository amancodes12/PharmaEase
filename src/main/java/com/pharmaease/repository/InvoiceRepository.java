package com.pharmaease.repository;

import com.pharmaease.model.Invoice;
import com.pharmaease.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByOrder(Orders order);
    List<Invoice> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(i.amountPaid) FROM Invoice i WHERE i.generatedAt BETWEEN :start AND :end")
    BigDecimal sumAmountPaidBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(i.amountPaid) FROM Invoice i")
    BigDecimal sumAmountPaidAll();
}