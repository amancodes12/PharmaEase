package com.pharmaease.service;

import com.pharmaease.model.Inventory;
import com.pharmaease.model.Medicine;
import com.pharmaease.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public Inventory getInventoryByMedicine(Medicine medicine) {
        return inventoryRepository.findByMedicine(medicine)
                .orElseThrow(() -> new RuntimeException("Inventory not found for medicine"));
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<Inventory> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems();
    }

    public Long getLowStockCount() {
        return inventoryRepository.countLowStockItems();
    }

    public void updateInventoryQuantity(Medicine medicine, int quantityChange) {
        Inventory inventory = getInventoryByMedicine(medicine);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantityChange);
        inventory.setTotalQuantity(inventory.getTotalQuantity() + quantityChange);
        inventory.setLowStock(inventory.getAvailableQuantity() <= medicine.getReorderLevel());
        inventoryRepository.save(inventory);
    }
}