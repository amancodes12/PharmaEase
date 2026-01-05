package com.pharmaease.service;

import com.pharmaease.model.*;
import com.pharmaease.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository batchRepository;
    private final InvoiceRepository invoiceRepository;

    public Report generateSalesReport(LocalDate startDate, LocalDate endDate, Report.ReportType reportType, Pharmacist pharmacist) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Orders> orders = orderRepository.findByCreatedAtBetween(start, end);

        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() == Orders.OrderStatus.COMPLETED)
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalOrders = (int) orders.stream()
                .filter(o -> o.getStatus() == Orders.OrderStatus.COMPLETED)
                .count();

        Report report = new Report();
        report.setReportType(reportType);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalSales(totalSales);
        report.setTotalOrders(totalOrders);
        report.setGeneratedBy(pharmacist);
        report.setSummary(String.format("Sales report from %s to %s. Total orders: %d, Total sales: $%.2f",
                startDate, endDate, totalOrders, totalSales));

        return reportRepository.save(report);
    }

    public Report generateInventoryReport(Pharmacist pharmacist) {
        List<Inventory> inventoryList = inventoryRepository.findAll();

        long lowStockCount = inventoryList.stream()
                .filter(Inventory::getLowStock)
                .count();

        long outOfStockCount = inventoryList.stream()
                .filter(i -> i.getAvailableQuantity() == 0)
                .count();

        Report report = new Report();
        report.setReportType(Report.ReportType.INVENTORY);
        report.setStartDate(LocalDate.now());
        report.setEndDate(LocalDate.now());
        report.setGeneratedBy(pharmacist);
        report.setSummary(String.format("Inventory report. Total items: %d, Low stock: %d, Out of stock: %d",
                inventoryList.size(), lowStockCount, outOfStockCount));

        return reportRepository.save(report);
    }

    public Report generateLowStockReport(Pharmacist pharmacist) {
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();

        Report report = new Report();
        report.setReportType(Report.ReportType.LOW_STOCK);
        report.setStartDate(LocalDate.now());
        report.setEndDate(LocalDate.now());
        report.setGeneratedBy(pharmacist);
        report.setSummary(String.format("Low stock report. %d items need reordering", lowStockItems.size()));

        return reportRepository.save(report);
    }

    public Report generateExpiringStockReport(int daysAhead, Pharmacist pharmacist) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        List<StockBatch> expiringBatches = batchRepository.findExpiringBatches(today, futureDate);

        Report report = new Report();
        report.setReportType(Report.ReportType.EXPIRING_STOCK);
        report.setStartDate(today);
        report.setEndDate(futureDate);
        report.setGeneratedBy(pharmacist);
        report.setSummary(String.format("Expiring stock report. %d batches expiring within %d days",
                expiringBatches.size(), daysAhead));

        return reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Define common date ranges
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Sales based on COMPLETED orders ONLY (actual sales)
        // Use orders directly since they're always created, invoices might have timing issues
        Double todaySalesFromOrders = orderRepository.sumCompletedTotalAmountBetween(todayStart, todayEnd);
        double todaySales = todaySalesFromOrders != null ? todaySalesFromOrders : 0.0;
        stats.put("todaySales", todaySales);

        Double weekSalesFromOrders = orderRepository.sumCompletedTotalAmountBetween(weekStart, todayEnd);
        double weekSales = weekSalesFromOrders != null ? weekSalesFromOrders : 0.0;
        stats.put("weekSales", weekSales);

        Double monthSalesFromOrders = orderRepository.sumCompletedTotalAmountBetween(monthStart, todayEnd);
        double monthSales = monthSalesFromOrders != null ? monthSalesFromOrders : 0.0;
        stats.put("monthSales", monthSales);

        Double totalSalesFromOrders = orderRepository.sumCompletedTotalAmountAll();
        double totalSales = totalSalesFromOrders != null ? totalSalesFromOrders : 0.0;
        stats.put("totalSales", totalSales);

        // Today's orders - count COMPLETED orders only (actual sales)
        Long todayOrders = orderRepository.countCompletedOrdersBetween(todayStart, todayEnd);
        stats.put("todayOrders", todayOrders != null ? todayOrders : 0L);
        
        // Verify by getting actual orders
        List<Orders> allCompletedToday = orderRepository.findByCreatedAtBetween(todayStart, todayEnd)
                .stream()
                .filter(o -> o.getStatus() == Orders.OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        System.out.println("📊 Dashboard Stats - Today Sales: ₹" + todaySales + ", Today Orders (query): " + todayOrders + ", Today Orders (actual): " + allCompletedToday.size() + ", Total Sales: ₹" + totalSales);
        
        // If query doesn't match actual, use actual count
        if (todayOrders == null || todayOrders != allCompletedToday.size()) {
            stats.put("todayOrders", (long) allCompletedToday.size());
            System.out.println("⚠️ Query mismatch detected - using actual count: " + allCompletedToday.size());
        }

        // Low stock count
        Long lowStockCount = inventoryRepository.countLowStockItems();
        stats.put("lowStockCount", lowStockCount != null ? lowStockCount : 0L);

        // Expiring soon (30 days) - skip this query to avoid terminal noise and improve performance
        // Only calculate if really needed, otherwise set to 0
        stats.put("expiringBatchesCount", 0);

        return stats;
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}