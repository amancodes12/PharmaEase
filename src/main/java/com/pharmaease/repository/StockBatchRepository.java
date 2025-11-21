package com.pharmaease.repository;

import com.pharmaease.model.Medicine;
import com.pharmaease.model.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {
    Optional<StockBatch> findByBatchNumber(String batchNumber);
    List<StockBatch> findByMedicine(Medicine medicine);
    List<StockBatch> findByMedicineAndActive(Medicine medicine, Boolean active);
    List<StockBatch> findByActive(Boolean active);

    @Query("SELECT b FROM StockBatch b WHERE b.expiryDate BETWEEN :startDate AND :endDate AND b.active = true")
    List<StockBatch> findExpiringBatches(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM StockBatch b WHERE b.expiryDate < :date AND b.active = true")
    List<StockBatch> findExpiredBatches(@Param("date") LocalDate date);

    @Query("SELECT b FROM StockBatch b WHERE b.medicine.id = :medicineId AND b.remainingQuantity > 0 AND b.active = true ORDER BY b.expiryDate ASC")
    List<StockBatch> findAvailableBatchesByMedicine(@Param("medicineId") Long medicineId);
}