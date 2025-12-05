package com.smiledev.bum.controller;

import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository; // Tiêm UserRepository

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication != null && authentication.isAuthenticated()) {
            // Lấy username từ đối tượng Authentication
            String username = authentication.getName();
            // Tìm thông tin đầy đủ của người dùng từ database
            Optional<Users> userOpt = userRepository.findByUsername(username);
            // Nếu tìm thấy, thêm đối tượng Users vào model
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        Optional<Products> mostPurchasedProductOpt = productService.findMostPurchasedProduct();
        mostPurchasedProductOpt.ifPresent(product -> model.addAttribute("mostPurchasedProduct", product));

        return "home";
    }
}
