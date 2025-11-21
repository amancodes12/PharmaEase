package com.pharmaease.service;

import com.pharmaease.model.Inventory;
import com.pharmaease.model.Medicine;
import com.pharmaease.model.StockBatch;
import com.pharmaease.repository.InventoryRepository;
import com.pharmaease.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockBatchService {

    private final StockBatchRepository batchRepository;
    private final InventoryRepository inventoryRepository;

    public StockBatch createBatch(StockBatch batch) {
        batch.setRemainingQuantity(batch.getQuantity());
        StockBatch saved = batchRepository.save(batch);

        // Update inventory
        updateInventoryAfterBatchCreation(saved);

        return saved;
    }

    public StockBatch updateBatch(Long id, StockBatch batch) {
        StockBatch existing = getBatchById(id);
        int quantityDifference = batch.getQuantity() - existing.getQuantity();

        existing.setBatchNumber(batch.getBatchNumber());
        existing.setQuantity(batch.getQuantity());
        existing.setRemainingQuantity(existing.getRemainingQuantity() + quantityDifference);
        existing.setCostPrice(batch.getCostPrice());
        existing.setManufacturingDate(batch.getManufacturingDate());
        existing.setExpiryDate(batch.getExpiryDate());
        existing.setActive(batch.getActive());

        StockBatch updated = batchRepository.save(existing);

        // Update inventory
        updateInventoryAfterBatchUpdate(updated, quantityDifference);

        return updated;
    }

    public void deleteBatch(Long id) {
        StockBatch batch = getBatchById(id);
        batchRepository.deleteById(id);

        // Update inventory
        updateInventoryAfterBatchDeletion(batch);
    }

    public StockBatch getBatchById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
    }

    public List<StockBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    public List<StockBatch> getBatchesByMedicine(Medicine medicine) {
        return batchRepository.findByMedicineAndActive(medicine, true);
    }

    public List<StockBatch> getExpiringBatches(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        return batchRepository.findExpiringBatches(today, futureDate);
    }

    public List<StockBatch> getExpiredBatches() {
        return batchRepository.findExpiredBatches(LocalDate.now());
    }

    private void updateInventoryAfterBatchCreation(StockBatch batch) {
        Inventory inventory = inventoryRepository.findByMedicine(batch.getMedicine())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setTotalQuantity(inventory.getTotalQuantity() + batch.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + batch.getQuantity());
        inventory.setLowStock(inventory.getAvailableQuantity() <= batch.getMedicine().getReorderLevel());

        inventoryRepository.save(inventory);
    }

    private void updateInventoryAfterBatchUpdate(StockBatch batch, int quantityDifference) {
        Inventory inventory = inventoryRepository.findByMedicine(batch.getMedicine())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setTotalQuantity(inventory.getTotalQuantity() + quantityDifference);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantityDifference);
        inventory.setLowStock(inventory.getAvailableQuantity() <= batch.getMedicine().getReorderLevel());

        inventoryRepository.save(inventory);
    }

    private void updateInventoryAfterBatchDeletion(StockBatch batch) {
        Inventory inventory = inventoryRepository.findByMedicine(batch.getMedicine())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setTotalQuantity(inventory.getTotalQuantity() - batch.getRemainingQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - batch.getRemainingQuantity());
        inventory.setLowStock(inventory.getAvailableQuantity() <= batch.getMedicine().getReorderLevel());

        inventoryRepository.save(inventory);
    }
}