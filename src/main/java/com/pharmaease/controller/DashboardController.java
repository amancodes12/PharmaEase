package com.pharmaease.controller;

import com.pharmaease.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ReportService reportService;

    @GetMapping
    public String dashboard(Model model) {
        try {
            System.out.println("🔄 Loading dashboard - fetching fresh statistics from database");
            Map<String, Object> stats = reportService.getDashboardStatistics();
            model.addAttribute("stats", stats);
            System.out.println("✅ Dashboard loaded successfully");
            return "dashboards";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading dashboard: " + e.getMessage());
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "dashboards";
        }
    }
}