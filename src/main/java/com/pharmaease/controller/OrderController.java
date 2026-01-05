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
            // Always get fresh orders directly from repository
            List<Orders> orders = orderService.getAllOrders();
            System.out.println("📋 Orders page - Total orders fetched: " + orders.size());
            
            if (status != null && !status.isEmpty()) {
                try {
                    Orders.OrderStatus orderStatus = Orders.OrderStatus.valueOf(status.toUpperCase());
                    orders = orders.stream()
                            .filter(o -> o.getStatus() == orderStatus)
                            .collect(java.util.stream.Collectors.toList());
                    System.out.println("📋 Filtered by " + status + ": " + orders.size() + " orders");
                } catch (IllegalArgumentException e) {
                    // Keep all orders if invalid status
                }
            }
            
            // Sort by most recent first
            orders.sort((o1, o2) -> {
                if (o1.getCreatedAt() == null) return 1;
                if (o2.getCreatedAt() == null) return -1;
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            });
            
            // Log order details
            orders.forEach(o -> System.out.println("  - Order #" + o.getOrderNumber() + " | Status: " + o.getStatus() + " | Amount: ₹" + o.getTotalAmount() + " | Date: " + o.getCreatedAt()));
            
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