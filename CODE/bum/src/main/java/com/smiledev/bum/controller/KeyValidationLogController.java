package com.smiledev.bum.controller;

import com.smiledev.bum.service.KeyValidationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/key-validation-logs")
public class KeyValidationLogController {

    @Autowired
    private KeyValidationLogService keyValidationLogService;

    // Add controller methods here
}
