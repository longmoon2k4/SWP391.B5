package com.smiledev.bum.service;

import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductReviewService {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Approve a pending product
     */
    @Transactional
    public void approveProduct(int productId, Users admin) {
        Optional<Products> productOpt = productsRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Products product = productOpt.get();
        
        if (product.getStatus() != Products.Status.pending) {
            throw new IllegalStateException("Product is not in pending status: " + product.getStatus());
        }

        // Update status
        product.setStatus(Products.Status.approved);
        product.setRejectionReason(null); // Clear any previous rejection reason
        productsRepository.save(product);

        // Log activity
        activityLogService.logActivity(
                admin,
                "APPROVE_PRODUCT",
                "Products",
                productId,
                "Product approved: " + product.getName()
        );
    }

    /**
     * Reject a pending product with reason
     */
    @Transactional
    public void rejectProduct(int productId, String rejectionReason, Users admin) {
        Optional<Products> productOpt = productsRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Products product = productOpt.get();
        
        if (product.getStatus() != Products.Status.pending) {
            throw new IllegalStateException("Product is not in pending status: " + product.getStatus());
        }

        // Validate rejection reason
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason cannot be empty");
        }

        // Update status
        product.setStatus(Products.Status.rejected);
        product.setRejectionReason(rejectionReason.trim());
        productsRepository.save(product);

        // Log activity
        activityLogService.logActivity(
                admin,
                "REJECT_PRODUCT",
                "Products",
                productId,
                "Product rejected: " + product.getName() + " - Reason: " + rejectionReason
        );
    }

    /**
     * Hide a product from marketplace
     */
    @Transactional
    public void hideProduct(int productId, String reason, Users admin) {
        Optional<Products> productOpt = productsRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Products product = productOpt.get();
        product.setStatus(Products.Status.hidden);
        product.setRejectionReason(reason);
        productsRepository.save(product);

        // Log activity
        activityLogService.logActivity(
                admin,
                "HIDE_PRODUCT",
                "Products",
                productId,
                "Product hidden: " + product.getName()
        );
    }

    /**
     * Restore a rejected product to pending status
     */
    @Transactional
    public void restoreProduct(int productId, Users admin) {
        Optional<Products> productOpt = productsRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Products product = productOpt.get();
        product.setStatus(Products.Status.pending);
        product.setRejectionReason(null);
        productsRepository.save(product);

        // Log activity
        activityLogService.logActivity(
                admin,
                "RESTORE_PRODUCT",
                "Products",
                productId,
                "Product restored to pending: " + product.getName()
        );
    }
}
