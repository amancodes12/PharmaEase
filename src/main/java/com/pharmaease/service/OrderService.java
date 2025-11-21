package com.pharmaease.service;

import com.pharmaease.model.*;
import com.pharmaease.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository batchRepository;
    private final InvoiceRepository invoiceRepository;

    public Orders createOrder(Orders order) {
        // Generate order number
        order.setOrderNumber(generateOrderNumber());

        // Validate stock availability
        for (OrderItem item : order.getOrderItems()) {
            validateStockAvailability(item.getMedicine(), item.getQuantity());
        }

        // Calculate totals
        calculateOrderTotals(order);

        // Save order
        Orders savedOrder = orderRepository.save(order);

        // Save order items
        for (OrderItem item : order.getOrderItems()) {
            item.setOrder(savedOrder);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemRepository.save(item);
        }

        // Update inventory
        updateInventoryForOrder(savedOrder);

        // Generate invoice if order is completed
        if (savedOrder.getStatus() == Orders.OrderStatus.COMPLETED) {
            generateInvoice(savedOrder);
        }

        return savedOrder;
    }

    public Orders completeOrder(Long orderId, BigDecimal amountPaid) {
        Orders order = getOrderById(orderId);

        if (order.getStatus() == Orders.OrderStatus.COMPLETED) {
            throw new RuntimeException("Order is already completed");
        }

        order.setStatus(Orders.OrderStatus.COMPLETED);
        order.setPaid(true);
        Orders completed = orderRepository.save(order);

        // Generate invoice
        generateInvoice(completed, amountPaid);

        return completed;
    }

    public Orders cancelOrder(Long orderId) {
        Orders order = getOrderById(orderId);

        if (order.getStatus() == Orders.OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed order");
        }

        order.setStatus(Orders.OrderStatus.CANCELLED);
        Orders cancelled = orderRepository.save(order);

        // Restore inventory
        restoreInventoryForCancelledOrder(cancelled);

        return cancelled;
    }

    public Orders getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Orders getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Orders> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Orders> getOrdersByStatus(Orders.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Orders> getRecentOrders(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findRecentOrders(startDate);
    }

    public List<Orders> getOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD-" + timestamp;
    }

    private void validateStockAvailability(Medicine medicine, int quantity) {
        Inventory inventory = inventoryRepository.findByMedicine(medicine)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for " + medicine.getName() +
                    ". Available: " + inventory.getAvailableQuantity() + ", Requested: " + quantity);
        }
    }

    private void calculateOrderTotals(Orders order) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        order.setSubtotal(subtotal);

        // Calculate tax (example: 5%)
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.05));
        order.setTax(tax);

        // Calculate total
        BigDecimal total = subtotal.add(tax).subtract(order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO);
        order.setTotalAmount(total);
    }

    private void updateInventoryForOrder(Orders order) {
        for (OrderItem item : order.getOrderItems()) {
            Inventory inventory = inventoryRepository.findByMedicine(item.getMedicine())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            // Deduct from available quantity
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.getQuantity());
            inventory.setLowStock(inventory.getAvailableQuantity() <= item.getMedicine().getReorderLevel());
            inventoryRepository.save(inventory);

            // Update batch quantities (FIFO)
            updateBatchQuantities(item.getMedicine(), item.getQuantity());
        }
    }

    private void updateBatchQuantities(Medicine medicine, int quantity) {
        List<StockBatch> batches = batchRepository.findAvailableBatchesByMedicine(medicine.getId());

        int remaining = quantity;
        for (StockBatch batch : batches) {
            if (remaining <= 0) break;

            if (batch.getRemainingQuantity() >= remaining) {
                batch.setRemainingQuantity(batch.getRemainingQuantity() - remaining);
                remaining = 0;
            } else {
                remaining -= batch.getRemainingQuantity();
                batch.setRemainingQuantity(0);
                batch.setActive(false);
            }
            batchRepository.save(batch);
        }
    }

    private void restoreInventoryForCancelledOrder(Orders order) {
        for (OrderItem item : order.getOrderItems()) {
            Inventory inventory = inventoryRepository.findByMedicine(item.getMedicine())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + item.getQuantity());
            inventory.setLowStock(inventory.getAvailableQuantity() <= item.getMedicine().getReorderLevel());
            inventoryRepository.save(inventory);
        }
    }

    private void generateInvoice(Orders order) {
        generateInvoice(order, order.getTotalAmount());
    }

    private void generateInvoice(Orders order, BigDecimal amountPaid) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setOrder(order);
        invoice.setAmountPaid(amountPaid);

        BigDecimal change = amountPaid.subtract(order.getTotalAmount());
        invoice.setChangeGiven(change.max(BigDecimal.ZERO));

        invoiceRepository.save(invoice);
    }

    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "INV-" + timestamp;
    }
}