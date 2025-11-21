package com.pharmaease.controller.api;

import com.pharmaease.model.StockBatch;
import com.pharmaease.service.StockBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockBatchRestController {

    private final StockBatchService batchService;

    @GetMapping
    public ResponseEntity<List<StockBatch>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockBatch> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<StockBatch>> getExpiringBatches(@RequestParam(defaultValue = "30") int daysAhead) {
        return ResponseEntity.ok(batchService.getExpiringBatches(daysAhead));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<StockBatch>> getExpiredBatches() {
        return ResponseEntity.ok(batchService.getExpiredBatches());
    }

    @PostMapping
    public ResponseEntity<StockBatch> createBatch(@RequestBody StockBatch batch) {
        return ResponseEntity.ok(batchService.createBatch(batch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockBatch> updateBatch(@PathVariable Long id, @RequestBody StockBatch batch) {
        return ResponseEntity.ok(batchService.updateBatch(id, batch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return ResponseEntity.noContent().build();
    }
}