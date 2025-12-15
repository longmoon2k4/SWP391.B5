package com.smiledev.bum.repository;

import com.smiledev.bum.entity.KeyValidationLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyValidationLogsRepository extends JpaRepository<KeyValidationLogs, Long> {
    List<KeyValidationLogs> findTop10ByOrderByRequestTimeDesc();
}
