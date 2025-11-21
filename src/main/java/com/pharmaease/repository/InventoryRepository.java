package com.pharmaease.repository;

import com.pharmaease.model.Inventory;
import com.pharmaease.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMedicine(Medicine medicine);
    List<Inventory> findByLowStock(Boolean lowStock);

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.medicine.reorderLevel")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity = 0")
    List<Inventory> findOutOfStockItems();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.lowStock = true")
    Long countLowStockItems();
}