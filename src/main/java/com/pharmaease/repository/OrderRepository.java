package com.pharmaease.repository;

import com.pharmaease.model.Customer;
import com.pharmaease.model.Orders;
import com.pharmaease.model.Pharmacist;
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
}