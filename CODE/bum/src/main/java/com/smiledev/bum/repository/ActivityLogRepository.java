package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ActivityLogs;
import com.smiledev.bum.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogs, Integer> {

	List<ActivityLogs> findTop5ByUserOrderByCreatedAtDesc(Users user);
}
