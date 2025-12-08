package com.smiledev.bum.service;

import com.smiledev.bum.dto.ProductCardDTO;
import com.smiledev.bum.dto.ProductPackageDTO;
import com.smiledev.bum.entity.ProductPackages;
import com.smiledev.bum.entity.Products;
import com.smiledev.bum.repository.ProductsRepository;
import com.smiledev.bum.repository.ReviewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ReviewsRepository reviewsRepository;

    public Optional<Products> findMostPurchasedProduct() {
        return productsRepository.findTopByOrderByTotalSalesDesc();
    }

    @Transactional(readOnly = true)
    public Page<ProductCardDTO> getApprovedProducts(Pageable pageable) {
        Page<Products> productPage = productsRepository.findAllApproved(pageable);
        return productPage.map(this::convertToProductCardDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductCardDTO> getApprovedProducts(Integer categoryId, String name, Pageable pageable) {
        Page<Products> productPage = productsRepository.findApprovedByCategoryIdAndName(categoryId, name, pageable);
        return productPage.map(this::convertToProductCardDTO);
    }

    @Transactional(readOnly = true)
    public Optional<Products> findProductById(int id) {
        // Sử dụng phương thức mới để tải tất cả dữ liệu cần thiết
        return productsRepository.findByIdWithDetails(id);
    }

    private ProductCardDTO convertToProductCardDTO(Products product) {
        ProductCardDTO dto = new ProductCardDTO();
        dto.setId(product.getProductId());
        dto.setName(product.getName());
        dto.setShortDescription(product.getShortDescription());
        dto.setDemoVideoUrl(product.getDemoVideoUrl());
        dto.setViewCount(product.getViewCount());

        Double avgRating = reviewsRepository.findAverageRatingByProductId(product.getProductId());
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);

        dto.setPackages(product.getPackages().stream()
                .map(this::convertToProductPackageDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private ProductPackageDTO convertToProductPackageDTO(ProductPackages productPackage) {
        ProductPackageDTO dto = new ProductPackageDTO();
        dto.setName(productPackage.getName());
        dto.setPrice(productPackage.getPrice());
        dto.setDurationDays(productPackage.getDurationDays());
        return dto;
    }
}
