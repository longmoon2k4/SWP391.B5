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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smiledev.bum.dto.ProductCardDTO;
import com.smiledev.bum.entity.Categories;
import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.CategoriesRepository;
import com.smiledev.bum.repository.UserRepository;
import com.smiledev.bum.service.ProductService;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model, Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductCardDTO> productPage = productService.getApprovedProducts(categoryId, search, pageable);
        model.addAttribute("productPage", productPage);

        Iterable<Categories> categories = categoriesRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);

        return "products";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable("id") int id, Model model, Authentication authentication) {
        // Thêm trạng thái đăng nhập để header hiển thị đúng
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Users> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }
        productService.incrementViewCount(id);
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
