package com.pharmaease.controller.api;

import com.pharmaease.model.Orders;
import com.pharmaease.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderRestController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Orders>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orders> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Orders> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Orders>> getOrdersByStatus(@PathVariable String status) {
        Orders.OrderStatus orderStatus = Orders.OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(orderService.getOrdersByStatus(orderStatus));
    }

    @PostMapping
    public ResponseEntity<Orders> createOrder(@RequestBody Orders order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Orders> completeOrder(@PathVariable Long id, @RequestParam BigDecimal amountPaid) {
        return ResponseEntity.ok(orderService.completeOrder(id, amountPaid));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Orders> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}