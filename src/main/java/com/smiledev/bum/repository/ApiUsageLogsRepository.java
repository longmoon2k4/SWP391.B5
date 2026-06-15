package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ApiUsageLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiUsageLogsRepository extends JpaRepository<ApiUsageLogs, Long> {
    List<ApiUsageLogs> findByApiKeyKeyId(Integer keyId);

    Page<ApiUsageLogs> findByApiKeyKeyId(Integer keyId, Pageable pageable);

    @Query("SELECT COUNT(l) FROM ApiUsageLogs l WHERE l.apiKey.keyId = :keyId AND l.requestTime >= :startTime")
    long countByKeyIdAndRequestTimeAfter(@Param("keyId") Integer keyId, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT AVG(l.responseTimeMs) FROM ApiUsageLogs l WHERE l.apiKey.keyId = :keyId")
    Double getAverageResponseTime(@Param("keyId") Integer keyId);
}
