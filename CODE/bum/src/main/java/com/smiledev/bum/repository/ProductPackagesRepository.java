package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ProductPackages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPackagesRepository extends JpaRepository<ProductPackages, Integer> {
}
