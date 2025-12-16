package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Orders;
import com.smiledev.bum.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUser(Users user);

    @Query("SELECT COUNT(DISTINCT o.orderId) FROM Orders o JOIN o.licenses l WHERE l.product.productId = :productId")
    long countByProductId(@Param("productId") Integer productId);
}
