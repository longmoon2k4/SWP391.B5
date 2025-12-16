package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Orders;
import com.smiledev.bum.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.math.BigDecimal;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUser(Users user);

    List<Orders> findTop5ByUserOrderByCreatedAtDesc(Users user);

    @Query("SELECT COUNT(DISTINCT o.orderId) FROM Orders o JOIN o.licenses l WHERE l.product.productId = :productId")
    long countByProductId(@Param("productId") Integer productId);

    // Đếm các order có status = 'completed' theo userid
    long countByUserAndStatus(Users user, Orders.Status status);

    // Đếm các order theo userid
    long countByUser(Users user);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Orders o WHERE o.user = :user AND o.status = :status")
    BigDecimal sumTotalAmountByUserAndStatus(@Param("user") Users user, @Param("status") Orders.Status status);
}
