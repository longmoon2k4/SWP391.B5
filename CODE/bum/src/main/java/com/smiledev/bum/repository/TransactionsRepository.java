package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {

    @Query("SELECT SUM(t.amount) FROM Transactions t WHERE t.type = :type")
    BigDecimal calculateTotalRevenue(@Param("type") Transactions.Type type);
}
