package com.pharmaease.repository;

import com.pharmaease.model.Medicine;
import com.pharmaease.model.OrderItem;
import com.pharmaease.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Orders order);
    List<OrderItem> findByMedicine(Medicine medicine);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.createdAt BETWEEN :start AND :end")
    List<OrderItem> findItemsSoldBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi.medicine, SUM(oi.quantity) as totalSold FROM OrderItem oi " +
            "WHERE oi.order.createdAt BETWEEN :start AND :end AND oi.order.status = 'COMPLETED' " +
            "GROUP BY oi.medicine ORDER BY totalSold DESC")
    List<Object[]> findTopSellingMedicines(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}