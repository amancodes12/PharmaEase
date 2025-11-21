package com.pharmaease.controller;

import com.pharmaease.model.Customer;
import com.pharmaease.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("customers", customerService.searchCustomers(search));
        } else {
            model.addAttribute("customers", customerService.getAllCustomers());
        }
        return "customers";
    }

    @GetMapping("/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer-form";
    }

    @GetMapping("/edit/{id}")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customer-form";
    }

    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute Customer customer,
                               RedirectAttributes redirectAttributes) {
        try {
            if (customer.getId() == null) {
                customerService.createCustomer(customer);
                redirectAttributes.addFlashAttribute("success", "Customer added successfully");
            } else {
                customerService.updateCustomer(customer.getId(), customer);
                redirectAttributes.addFlashAttribute("success", "Customer updated successfully");
            }
            return "redirect:/customers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customers/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }
}