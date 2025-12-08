package com.smiledev.bum.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smiledev.bum.dto.ProductCardDTO;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ProductService;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model, Authentication authentication,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "8") int size) {

        // Lấy thông tin người dùng đăng nhập
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        // Lấy danh sách sản phẩm đã duyệt và phân trang
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductCardDTO> productPage = productService.getApprovedProducts(pageable);

        model.addAttribute("productPage", productPage);

        return "Home"; // Trả về view Home.html
    }
}
