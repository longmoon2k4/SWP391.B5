package com.smiledev.bum.controller;

import com.smiledev.bum.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/activity-logs")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    // Add controller methods here
}
