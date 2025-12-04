package com.smiledev.bum.controller;

import com.smiledev.bum.service.ProductVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/product-versions")
public class ProductVersionController {

    @Autowired
    private ProductVersionService productVersionService;

    // Add controller methods here
}
