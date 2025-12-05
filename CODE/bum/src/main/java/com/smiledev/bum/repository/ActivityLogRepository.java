package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ActivityLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogs, Integer> {
}
