package com.smiledev.bum.controller;

import com.smiledev.bum.entity.Products;
import com.smiledev.bum.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        Optional<Products> mostPurchasedProductOpt = productService.findMostPurchasedProduct();
        mostPurchasedProductOpt.ifPresent(product -> model.addAttribute("mostPurchasedProduct", product));

        return "home";
    }
}
