package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ProductVersions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVersionsRepository extends JpaRepository<ProductVersions, Integer> {
}
