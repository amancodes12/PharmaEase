package com.pharmaease.controller.api;

import com.pharmaease.model.Inventory;
import com.pharmaease.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryRestController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Inventory>> getOutOfStockItems() {
        return ResponseEntity.ok(inventoryService.getOutOfStockItems());
    }

    @GetMapping("/count/low-stock")
    public ResponseEntity<Long> getLowStockCount() {
        return ResponseEntity.ok(inventoryService.getLowStockCount());
    }
}