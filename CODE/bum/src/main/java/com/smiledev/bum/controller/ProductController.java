package com.smiledev.bum.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ProductService;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable("id") int id, Model model, Authentication authentication) {
        // Thêm trạng thái đăng nhập để header hiển thị đúng
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        Optional<Products> productOpt = productService.findProductById(id);

        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            // Tăng lượt xem ở đây (sẽ làm sau nếu cần)
            return "product"; // Trả về file product.html
        } else {
            return "redirect:/"; // Nếu không tìm thấy, quay về trang chủ
        }
    }
}
