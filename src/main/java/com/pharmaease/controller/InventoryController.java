package com.pharmaease.controller;

import com.pharmaease.service.InventoryService;
import com.pharmaease.service.StockBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final StockBatchService batchService;

    @GetMapping
    public String viewInventory(@RequestParam(required = false) String filter, Model model) {
        if ("low-stock".equals(filter)) {
            model.addAttribute("inventory", inventoryService.getLowStockItems());
            model.addAttribute("pageTitle", "Low Stock Items");
        } else if ("out-of-stock".equals(filter)) {
            model.addAttribute("inventory", inventoryService.getOutOfStockItems());
            model.addAttribute("pageTitle", "Out of Stock Items");
        } else {
            model.addAttribute("inventory", inventoryService.getAllInventory());
            model.addAttribute("pageTitle", "All Inventory");
        }

        model.addAttribute("lowStockCount", inventoryService.getLowStockCount());
        return "inventory";
    }

    @GetMapping("/alerts")
    public String viewAlerts(Model model) {
        model.addAttribute("lowStock", inventoryService.getLowStockItems());
        model.addAttribute("expiringBatches", batchService.getExpiringBatches(30));
        model.addAttribute("expiredBatches", batchService.getExpiredBatches());
        return "inventory-alerts";
    }
}