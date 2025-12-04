package com.smiledev.bum.service;

import com.smiledev.bum.entity.Products;
import com.smiledev.bum.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductsRepository productsRepository;

    public Optional<Products> findMostPurchasedProduct() {
        return productsRepository.findTopByOrderByTotalSalesDesc();
    }
}
