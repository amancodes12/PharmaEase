    package com.pharmaease.controller.api;

    import com.pharmaease.service.ReportService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.Map;

    @RestController
    @RequestMapping("/api/dashboard")
    @RequiredArgsConstructor
    @CrossOrigin(origins = "*")
    public class DashboardRestController {

        private final ReportService reportService;

        @GetMapping("/statistics")
        public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
            return ResponseEntity.ok(reportService.getDashboardStatistics());
        }
    }