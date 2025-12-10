package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ProductPackages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductPackagesRepository extends JpaRepository<ProductPackages, Integer> {
    // Add the missing method
    List<ProductPackages> findByPrice(BigDecimal price);
}
