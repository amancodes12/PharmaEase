package com.pharmaease.controller;

import com.pharmaease.model.Orders;
import com.pharmaease.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final MedicineService medicineService;
    private final PharmacistService pharmacistService;

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status, Model model) {
        if (status != null && !status.isEmpty()) {
            model.addAttribute("orders", orderService.getOrdersByStatus(Orders.OrderStatus.valueOf(status)));
        } else {
            model.addAttribute("orders", orderService.getAllOrders());
        }
        return "orders";
    }

    @GetMapping("/new")
    public String newOrderForm(Model model) {
        model.addAttribute("order", new Orders());
        model.addAttribute("customers", customerService.getActiveCustomers());
        model.addAttribute("medicines", medicineService.getActiveMedicines());
        return "order-form";
    }

    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        return "order-details";
    }

    @PostMapping("/complete/{id}")
    public String completeOrder(@PathVariable Long id,
                                @RequestParam BigDecimal amountPaid,
                                RedirectAttributes redirectAttributes) {
        try {
            orderService.completeOrder(id, amountPaid);
            redirectAttributes.addFlashAttribute("success", "Order completed successfully");
            return "redirect:/orders/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/view/" + id;
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/view/" + id;
    }
}