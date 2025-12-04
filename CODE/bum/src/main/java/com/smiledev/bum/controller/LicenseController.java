package com.smiledev.bum.controller;

import com.smiledev.bum.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/licenses")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    // Add controller methods here
}
