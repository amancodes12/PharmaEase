package com.pharmaease.repository;

import com.pharmaease.model.Customer;
import com.pharmaease.model.Orders;
import com.pharmaease.model.Pharmacist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByOrderNumber(String orderNumber);
    List<Orders> findByCustomer(Customer customer);
    List<Orders> findByPharmacist(Pharmacist pharmacist);
    List<Orders> findByStatus(Orders.OrderStatus status);
    List<Orders> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Orders o WHERE o.createdAt >= :startDate ORDER BY o.createdAt DESC")
    List<Orders> findRecentOrders(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.createdAt BETWEEN :start AND :end AND o.status <> 'CANCELLED'")
    Long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.createdAt BETWEEN :start AND :end AND o.status = 'COMPLETED'")
    Long countCompletedOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.createdAt BETWEEN :start AND :end AND o.status <> 'CANCELLED'")
    Double sumTotalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.status <> 'CANCELLED'")
    Double sumTotalAmountAll();

    @Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.createdAt BETWEEN :start AND :end AND o.status = 'COMPLETED'")
    Double sumCompletedTotalAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.status = 'COMPLETED'")
    Double sumCompletedTotalAmountAll();
    
    // Eagerly fetch customer and pharmacist to avoid lazy loading issues
    // Using EntityGraph for better performance and to avoid DISTINCT issues
    // Note: orderItems are loaded via cascade, so we don't need to include them in EntityGraph
    @EntityGraph(attributePaths = {"customer", "pharmacist", "invoice"})
    @Query("SELECT DISTINCT o FROM Orders o ORDER BY o.createdAt DESC")
    List<Orders> findAllWithRelations();
    
    @EntityGraph(attributePaths = {"customer", "pharmacist", "invoice"})
    @Query("SELECT DISTINCT o FROM Orders o WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<Orders> findByStatusWithRelations(@Param("status") Orders.OrderStatus status);
    
    @EntityGraph(attributePaths = {"customer", "pharmacist", "invoice"})
    @Query("SELECT DISTINCT o FROM Orders o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    List<Orders> findByCreatedAtBetweenWithRelations(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT DISTINCT o FROM Orders o " +
           "LEFT JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.pharmacist " +
           "LEFT JOIN FETCH o.invoice " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.medicine " +
           "WHERE o.id = :id")
    Optional<Orders> findByIdWithRelations(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"customer", "pharmacist", "invoice"})
    @Query("SELECT o FROM Orders o WHERE o.orderNumber = :orderNumber")
    Optional<Orders> findByOrderNumberWithRelations(@Param("orderNumber") String orderNumber);
}