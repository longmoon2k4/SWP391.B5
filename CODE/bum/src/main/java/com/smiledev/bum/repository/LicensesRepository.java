package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Licenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicensesRepository extends JpaRepository<Licenses, Integer> {
    // Find all licenses associated with a specific order ID
    List<Licenses> findByOrder_OrderId(int orderId);
}
