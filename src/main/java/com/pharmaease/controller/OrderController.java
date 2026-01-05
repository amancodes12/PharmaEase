package com.pharmaease.controller;

import com.pharmaease.model.Orders;
import com.pharmaease.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

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
        try {
            List<Orders> orders;
            
            if (status != null && !status.isEmpty()) {
                try {
                    Orders.OrderStatus orderStatus = Orders.OrderStatus.valueOf(status.toUpperCase());
                    orders = orderService.getOrdersByStatus(orderStatus);
                } catch (IllegalArgumentException e) {
                    orders = orderService.getAllOrders();
                }
            } else {
                // Get ALL orders including COMPLETED ones from billing
                orders = orderService.getAllOrders();
            }
            
            // Sort by most recent first
            orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
            
            model.addAttribute("orders", orders);
            return "orders";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading orders: " + e.getMessage());
            model.addAttribute("orders", List.of());
            return "orders";
        }
    }

    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        try {
            Orders order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            return "order-details";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/orders";
        }
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