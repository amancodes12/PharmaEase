package com.pharmaease.controller;

import com.pharmaease.model.*;
import com.pharmaease.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;

@Controller
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final MedicineService medicineService;
    private final PharmacistService pharmacistService;
    private final BillingService billingService;

    @GetMapping
    public String billingPage(Model model) {
        model.addAttribute("customers", customerService.getActiveCustomers());
        model.addAttribute("medicines", medicineService.getActiveMedicines());
        return "billing";
    }

    @PostMapping("/create-order")
    public String createOrder(@ModelAttribute Orders order,
                              @RequestParam(required = false) Long customerId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            // Set customer if provided
            if (customerId != null) {
                order.setCustomer(customerService.getCustomerById(customerId));
            }

            // Set pharmacist from authenticated user
            String email = authentication.getName();
            Pharmacist pharmacist = pharmacistService.getPharmacistByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Pharmacist not found"));
            order.setPharmacist(pharmacist);

            // Initialize order items list if null
            if (order.getOrderItems() == null) {
                order.setOrderItems(new ArrayList<>());
            }

            // Create order as pending
            Orders createdOrder = orderService.createOrder(order);

            // Immediately complete the order using the calculated total to ensure
            // sales and invoices are recorded for the dashboard.
            createdOrder = orderService.completeOrder(createdOrder.getId(), createdOrder.getTotalAmount());

            redirectAttributes.addFlashAttribute("success", "Order created successfully");
            return "redirect:/billing/invoice/" + createdOrder.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/billing";
        }
    }

    @GetMapping("/invoice/{orderId}")
    public String viewInvoice(@PathVariable Long orderId, Model model) {
        try {
            Orders order = orderService.getOrderById(orderId);
            Invoice invoice = billingService.getInvoiceByOrder(order);
            model.addAttribute("order", order);
            model.addAttribute("invoice", invoice);
            return "invoice";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/billing";
        }
    }

    @GetMapping("/invoices")
    public String listInvoices(Model model) {
        model.addAttribute("invoices", billingService.getAllInvoices());
        return "invoices";
    }
}