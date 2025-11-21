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

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository batchRepository;

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

        // Today's sales
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        Double todaySales = orderRepository.sumTotalAmountBetween(todayStart, todayEnd);
        stats.put("todaySales", todaySales != null ? todaySales : 0.0);

        // This week's sales
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        Double weekSales = orderRepository.sumTotalAmountBetween(weekStart, todayEnd);
        stats.put("weekSales", weekSales != null ? weekSales : 0.0);

        // This month's sales
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Double monthSales = orderRepository.sumTotalAmountBetween(monthStart, todayEnd);
        stats.put("monthSales", monthSales != null ? monthSales : 0.0);

        // Today's orders
        Long todayOrders = orderRepository.countOrdersBetween(todayStart, todayEnd);
        stats.put("todayOrders", todayOrders != null ? todayOrders : 0L);

        // Low stock count
        Long lowStockCount = inventoryRepository.countLowStockItems();
        stats.put("lowStockCount", lowStockCount != null ? lowStockCount : 0L);

        // Expiring soon (30 days)
        List<StockBatch> expiringBatches = batchRepository.findExpiringBatches(
                LocalDate.now(), LocalDate.now().plusDays(30));
        stats.put("expiringBatchesCount", expiringBatches.size());

        return stats;
    }
}