package com.smiledev.bum.service;

import com.smiledev.bum.entity.ActivityLogs;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.ActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void logActivity(Users user, String actionType, String targetTable, Integer targetId, String description) {
        ActivityLogs log = new ActivityLogs();
        log.setUser(user);
        log.setActionType(actionType);
        log.setTargetTable(targetTable);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setIpAddress(getClientIpAddress());

        activityLogRepository.save(log);
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return "Unknown";
    }
}
