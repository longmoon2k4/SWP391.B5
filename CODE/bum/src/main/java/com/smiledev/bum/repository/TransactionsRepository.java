package com.smiledev.bum.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smiledev.bum.entity.Transactions;
import com.smiledev.bum.entity.Users;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {

    @Query("SELECT SUM(t.amount) FROM Transactions t WHERE t.type = :type")
    BigDecimal calculateTotalRevenue(@Param("type") Transactions.Type type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transactions t WHERE t.user = :user AND t.type = :type")
    BigDecimal sumByUserAndType(@Param("user") Users user, @Param("type") Transactions.Type type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transactions t WHERE t.user = :user AND t.type = :type AND t.createdAt BETWEEN :start AND :end")
    BigDecimal sumByUserTypeAndDateRange(@Param("user") Users user,
                                         @Param("type") Transactions.Type type,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    Page<Transactions> findByUserAndTypeOrderByCreatedAtDesc(Users user, Transactions.Type type, Pageable pageable);
}
