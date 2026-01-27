package com.pharmaease.service;

import com.pharmaease.model.*;
import com.pharmaease.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository batchRepository;
    private final InvoiceRepository invoiceRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Orders createOrder(Orders order) {
        // Generate order number if not already set
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(generateOrderNumber());
        }

        // Validate order items exist
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new RuntimeException("Order must have at least one item");
        }

        // Validate stock availability
        for (OrderItem item : order.getOrderItems()) {
            if (item.getMedicine() == null) {
                throw new RuntimeException("Order item must have a medicine");
            }
            // Only validate stock if order is being completed (not for pending orders)
            if (order.getStatus() == Orders.OrderStatus.COMPLETED) {
                validateStockAvailability(item.getMedicine(), item.getQuantity());
            }
        }

        // Calculate totals
        calculateOrderTotals(order);

        // Set default status if not set
        if (order.getStatus() == null) {
            order.setStatus(Orders.OrderStatus.PENDING);
        }
        
        // Set default payment method if not set
        if (order.getPaymentMethod() == null) {
            order.setPaymentMethod(Orders.PaymentMethod.CASH);
        }
        
        // Set default paid status
        if (order.getPaid() == null) {
            order.setPaid(order.getStatus() == Orders.OrderStatus.COMPLETED);
        }
        
        // Ensure createdAt is set (should be automatic with @CreationTimestamp, but ensure it's set)
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }

        // Save order first - FLUSH to ensure it's immediately available
        Orders savedOrder = orderRepository.saveAndFlush(order);
        System.out.println("✅ Order saved - ID: " + savedOrder.getId() + 
                         " | Status: " + savedOrder.getStatus() + 
                         " | CreatedAt: " + savedOrder.getCreatedAt() + 
                         " | Total: ₹" + savedOrder.getTotalAmount());
        System.out.println("✅ Order saved to database - ID: " + savedOrder.getId() + ", Status: " + savedOrder.getStatus());

        // Save order items with proper relationships
        for (OrderItem item : order.getOrderItems()) {
            item.setOrder(savedOrder);
            if (item.getTotalPrice() == null) {
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            orderItemRepository.saveAndFlush(item);
        }
        System.out.println("✅ Order items saved: " + order.getOrderItems().size());

        // Update inventory (deduct stock) - only if order is completed
        if (savedOrder.getStatus() == Orders.OrderStatus.COMPLETED) {
            System.out.println("✅ Order is COMPLETED - updating inventory and generating invoice");
            try {
                updateInventoryForOrder(savedOrder);
            } catch (Exception e) {
                // Log but don't fail the order if inventory update fails
                System.err.println("Warning: Inventory update failed: " + e.getMessage());
            }
            
            // Generate invoice for completed orders
            try {
                generateInvoice(savedOrder, savedOrder.getTotalAmount());
            } catch (Exception e) {
                // Log but don't fail the order if invoice generation fails
                System.err.println("Warning: Invoice generation failed: " + e.getMessage());
                // Try to generate invoice again with order total
                try {
                    generateInvoice(savedOrder, savedOrder.getTotalAmount());
                } catch (Exception e2) {
                    System.err.println("Error: Failed to generate invoice after retry: " + e2.getMessage());
                }
            }
        }

        // Force flush to ensure order is persisted immediately
        entityManager.flush();
        
        // Refresh to get latest state including invoice
        entityManager.refresh(savedOrder);
        
        // Ensure orderItems are loaded
        if (savedOrder.getOrderItems() != null) {
            savedOrder.getOrderItems().size(); // Force initialization
        }
        
        // Return the saved and flushed order
        System.out.println("✅ Final order status: " + savedOrder.getStatus() + ", Total: ₹" + savedOrder.getTotalAmount() + ", Created: " + savedOrder.getCreatedAt());
        System.out.println("✅ Order ID: " + savedOrder.getId());
        System.out.println("✅ Order has invoice: " + (savedOrder.getInvoice() != null ? savedOrder.getInvoice().getInvoiceNumber() : "null"));
        System.out.println("✅ Order has " + (savedOrder.getOrderItems() != null ? savedOrder.getOrderItems().size() : 0) + " items");

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
        return orderRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Orders getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumberWithRelations(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Orders> getAllOrders() {
        // Customer and pharmacist are now EAGER, so they'll be loaded automatically
        try {
            List<Orders> orders = orderRepository.findAllWithRelations();
            System.out.println("✅ Fetched " + orders.size() + " orders with relationships");
            return orders;
        } catch (Exception e) {
            System.err.println("❌ Error fetching orders: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<Orders> getOrdersByStatus(Orders.OrderStatus status) {
        return orderRepository.findByStatusWithRelations(status);
    }

    public List<Orders> getRecentOrders(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findRecentOrders(startDate);
    }

    public List<Orders> getOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetweenWithRelations(start, end);
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

            // Update batch quantities (FIFO) - wrapped in try-catch to not fail order
            try {
                updateBatchQuantities(item.getMedicine(), item.getQuantity());
            } catch (Exception e) {
                // Continue even if batch update fails
                System.err.println("Warning: Batch update failed for medicine " + item.getMedicine().getId() + ": " + e.getMessage());
            }
        }
    }

    private void updateBatchQuantities(Medicine medicine, int quantity) {
        try {
            List<StockBatch> batches = batchRepository.findAvailableBatchesByMedicine(medicine.getId());
            
            if (batches == null || batches.isEmpty()) {
                // No batches available - order can still proceed, just skip batch update
                return;
            }

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
        } catch (Exception e) {
            // If batch update fails, log but don't fail the order
            // Orders can still be created without batch tracking
            System.err.println("Warning: Failed to update batch quantities: " + e.getMessage());
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
        // Check if invoice already exists for this order
        Optional<Invoice> existingInvoice = invoiceRepository.findByOrder(order);
        if (existingInvoice.isPresent()) {
            // Invoice already exists, don't create duplicate
            System.out.println("ℹ️ Invoice already exists for order: " + order.getOrderNumber());
            return;
        }
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setOrder(order);
        invoice.setAmountPaid(amountPaid != null ? amountPaid : order.getTotalAmount());

        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = invoice.getAmountPaid();
        BigDecimal change = paid.subtract(total);
        invoice.setChangeGiven(change.max(BigDecimal.ZERO));

        // Save invoice and flush to ensure it's persisted
        Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
        
        // Set the invoice on the order to maintain bidirectional relationship
        order.setInvoice(savedInvoice);
        orderRepository.saveAndFlush(order);
        
        System.out.println("✅ Invoice generated: " + savedInvoice.getInvoiceNumber() + " for order: " + order.getOrderNumber());
    }

    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "INV-" + timestamp;
    }
}