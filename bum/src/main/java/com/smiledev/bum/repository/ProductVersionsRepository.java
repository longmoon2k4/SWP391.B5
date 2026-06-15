package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ProductVersions;
import com.smiledev.bum.entity.ProductVersions.VirusScanStatus;
import com.smiledev.bum.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVersionsRepository extends JpaRepository<ProductVersions, Integer> {
    List<ProductVersions> findByVirusScanStatus(VirusScanStatus status);

    Optional<ProductVersions> findByProductAndIsCurrentVersionTrue(Products product);
}
