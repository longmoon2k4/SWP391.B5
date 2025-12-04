package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {

    // Lấy sản phẩm có số lượng bán ra lớn nhất TOP 1
    Optional<Products> findTopByOrderByTotalSalesDesc();
}
