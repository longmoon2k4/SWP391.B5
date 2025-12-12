package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Orders;
import com.smiledev.bum.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUser(Users user);
}
