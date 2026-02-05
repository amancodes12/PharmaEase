package com.pharmaease.config;

import com.pharmaease.service.InventoryService;
import com.pharmaease.service.StockBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationControllerAdvice {

    private final InventoryService inventoryService;
    private final StockBatchService batchService;

    @ModelAttribute("notificationCount")
    public Long getNotificationCount() {
        try {
            // Count low stock items
            Long lowStockCount = inventoryService.getLowStockCount();
            
            // Count expiring batches (next 30 days)
            int expiringCount = batchService.getExpiringBatches(30).size();
            
            // Count expired batches
            int expiredCount = batchService.getExpiredBatches().size();
            
            // Total notification count
            return (lowStockCount != null ? lowStockCount : 0L) + expiringCount + expiredCount;
        } catch (Exception e) {
            // Return 0 if there's any error to prevent page loading issues
            return 0L;
        }
    }
}

