package com.smiledev.bum.controller;

import com.smiledev.bum.entity.Products;
import com.smiledev.bum.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable("id") int id, Model model) {
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
