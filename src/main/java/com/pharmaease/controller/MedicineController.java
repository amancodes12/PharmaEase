package com.pharmaease.controller;

import com.pharmaease.model.Medicine;
import com.pharmaease.service.MedicineService;
import com.pharmaease.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;
    private final SupplierService supplierService;

    @GetMapping
    public String listMedicines(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("medicines", medicineService.searchMedicines(search));
        } else {
            model.addAttribute("medicines", medicineService.getAllMedicines());
        }
        model.addAttribute("categories", medicineService.getAllCategories());
        model.addAttribute("manufacturers", medicineService.getAllManufacturers());
        return "medicines";
    }

    @GetMapping("/new")
    public String newMedicineForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        model.addAttribute("suppliers", supplierService.getActiveSuppliers());
        model.addAttribute("categories", medicineService.getAllCategories());
        return "medicine-form";
    }

    @GetMapping("/edit/{id}")
    public String editMedicineForm(@PathVariable Long id, Model model) {
        model.addAttribute("medicine", medicineService.getMedicineById(id));
        model.addAttribute("suppliers", supplierService.getActiveSuppliers());
        model.addAttribute("categories", medicineService.getAllCategories());
        return "medicine-form";
    }

    @PostMapping("/save")
    public String saveMedicine(@ModelAttribute Medicine medicine,
                               RedirectAttributes redirectAttributes) {
        try {
            // Handle supplier relationship - load the actual supplier entity if ID is set
            if (medicine.getSupplier() != null && medicine.getSupplier().getId() != null) {
                try {
                    medicine.setSupplier(supplierService.getSupplierById(medicine.getSupplier().getId()));
                } catch (Exception e) {
                    medicine.setSupplier(null);
                }
            } else {
                medicine.setSupplier(null);
            }
            
            if (medicine.getId() == null) {
                medicineService.createMedicine(medicine);
                redirectAttributes.addFlashAttribute("success", "Medicine added successfully");
            } else {
                medicineService.updateMedicine(medicine.getId(), medicine);
                redirectAttributes.addFlashAttribute("success", "Medicine updated successfully");
            }
            return "redirect:/medicines";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/medicines/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medicineService.deleteMedicine(id);
            redirectAttributes.addFlashAttribute("success", "Medicine deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/medicines";
    }
}