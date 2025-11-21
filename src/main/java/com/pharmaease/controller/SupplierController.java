package com.pharmaease.controller;

import com.pharmaease.model.Supplier;
import com.pharmaease.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public String listSuppliers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("suppliers", supplierService.searchSuppliers(search));
        } else {
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
        }
        return "suppliers";
    }

    @GetMapping("/new")
    public String newSupplierForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "supplier-form";
    }

    @GetMapping("/edit/{id}")
    public String editSupplierForm(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", supplierService.getSupplierById(id));
        return "supplier-form";
    }

    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute Supplier supplier,
                               RedirectAttributes redirectAttributes) {
        try {
            if (supplier.getId() == null) {
                supplierService.createSupplier(supplier);
                redirectAttributes.addFlashAttribute("success", "Supplier added successfully");
            } else {
                supplierService.updateSupplier(supplier.getId(), supplier);
                redirectAttributes.addFlashAttribute("success", "Supplier updated successfully");
            }
            return "redirect:/suppliers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/suppliers/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", "Supplier deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suppliers";
    }
}