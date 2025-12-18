package com.pharmaease.controller;

import com.pharmaease.model.StockBatch;
import com.pharmaease.service.MedicineService;
import com.pharmaease.service.StockBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/batches")
@RequiredArgsConstructor
public class StockBatchController {

    private final StockBatchService batchService;
    private final MedicineService medicineService;

    @GetMapping
    public String listBatches(@RequestParam(required = false) String filter, Model model) {
        if ("expiring".equals(filter)) {
            model.addAttribute("batches", batchService.getExpiringBatches(30));
            model.addAttribute("pageTitle", "Expiring Batches (30 days)");
        } else if ("expired".equals(filter)) {
            model.addAttribute("batches", batchService.getExpiredBatches());
            model.addAttribute("pageTitle", "Expired Batches");
        } else {
            model.addAttribute("batches", batchService.getAllBatches());
            model.addAttribute("pageTitle", "All Batches");
        }
        return "batches";
    }

    @GetMapping("/new")
    public String newBatchForm(@RequestParam(required = false) Long medicineId, Model model) {
        StockBatch batch = new StockBatch();
        if (medicineId != null) {
            batch.setMedicine(medicineService.getMedicineById(medicineId));
        }
        model.addAttribute("batch", batch);
        model.addAttribute("medicines", medicineService.getActiveMedicines());
        return "batch-form";
    }

    @GetMapping("/edit/{id}")
    public String editBatchForm(@PathVariable Long id, Model model) {
        model.addAttribute("batch", batchService.getBatchById(id));
        model.addAttribute("medicines", medicineService.getActiveMedicines());
        return "batch-form";
    }

    @PostMapping("/save")
    public String saveBatch(@ModelAttribute StockBatch batch,
                            RedirectAttributes redirectAttributes) {
        try {
            if (batch.getId() == null) {
                batchService.createBatch(batch);
                redirectAttributes.addFlashAttribute("success", "Batch added successfully");
            } else {
                batchService.updateBatch(batch.getId(), batch);
                redirectAttributes.addFlashAttribute("success", "Batch updated successfully");
            }
            return "redirect:/batches";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/batches/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBatch(@PathVariable Long id, 
                             @RequestParam(required = false) String redirectTo,
                             RedirectAttributes redirectAttributes) {
        try {
            batchService.deleteBatch(id);
            redirectAttributes.addFlashAttribute("success", "Batch deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        if ("alerts".equals(redirectTo)) {
            return "redirect:/inventory/alerts";
        }
        return "redirect:/batches";
    }
}