package com.pharmaease.controller;

import com.pharmaease.model.Pharmacist;
import com.pharmaease.model.Report;
import com.pharmaease.service.InventoryService;
import com.pharmaease.service.PharmacistService;
import com.pharmaease.service.ReportService;
import com.pharmaease.service.StockBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final PharmacistService pharmacistService;
    private final InventoryService inventoryService;
    private final StockBatchService batchService;

    @GetMapping
    public String reportsPage(Model model) {
        model.addAttribute("reports", reportService.getAllReports());
        return "reports";
    }

    @GetMapping("/generate")
    public String generateReportForm(Model model) {
        return "report-generate";
    }

    @PostMapping("/generate/sales")
    public String generateSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String reportType,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            Pharmacist pharmacist = pharmacistService.getPharmacistByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

            Report report = reportService.generateSalesReport(
                    startDate, endDate, Report.ReportType.valueOf(reportType), pharmacist);

            redirectAttributes.addFlashAttribute("success", "Sales report generated successfully");
            return "redirect:/reports/view/" + report.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reports/generate";
        }
    }

    @PostMapping("/generate/inventory")
    public String generateInventoryReport(Authentication authentication,
                                          RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            Pharmacist pharmacist = pharmacistService.getPharmacistByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

            Report report = reportService.generateInventoryReport(pharmacist);

            redirectAttributes.addFlashAttribute("success", "Inventory report generated successfully");
            return "redirect:/reports/view/" + report.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reports/generate";
        }
    }

    @PostMapping("/generate/low-stock")
    public String generateLowStockReport(Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            Pharmacist pharmacist = pharmacistService.getPharmacistByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

            Report report = reportService.generateLowStockReport(pharmacist);

            redirectAttributes.addFlashAttribute("success", "Low stock report generated successfully");
            return "redirect:/reports/view/" + report.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reports/generate";
        }
    }

    @PostMapping("/generate/expiring")
    public String generateExpiringStockReport(@RequestParam int daysAhead,
                                              Authentication authentication,
                                              RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            Pharmacist pharmacist = pharmacistService.getPharmacistByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

            Report report = reportService.generateExpiringStockReport(daysAhead, pharmacist);

            redirectAttributes.addFlashAttribute("success", "Expiring stock report generated successfully");
            return "redirect:/reports/view/" + report.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reports/generate";
        }
    }

    @GetMapping("/view/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        Report report = reportService.getReportById(id);
        model.addAttribute("report", report);

        // Add additional data based on report type
        if (report.getReportType() == Report.ReportType.LOW_STOCK) {
            model.addAttribute("lowStockItems", inventoryService.getLowStockItems());
        } else if (report.getReportType() == Report.ReportType.EXPIRING_STOCK) {
            model.addAttribute("expiringBatches",
                    batchService.getExpiringBatches(
                            (int) java.time.temporal.ChronoUnit.DAYS.between(report.getStartDate(), report.getEndDate())));
        }

        return "report-details";
    }
}