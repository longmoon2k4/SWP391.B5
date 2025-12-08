package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {

    // Lấy sản phẩm có số lượng bán ra lớn nhất TOP 1
    Optional<Products> findTopByOrderByTotalSalesDesc();

    // Lấy tất cả sản phẩm có status = 'approved' với phân trang
    @Query("SELECT p FROM Products p WHERE p.status = 'approved'")
    Page<Products> findAllApproved(Pageable pageable);

    // Lấy sản phẩm theo ID và tải sẵn TẤT CẢ các collection và entity liên quan cần thiết
    @Query("SELECT p FROM Products p " +
           "LEFT JOIN FETCH p.developer " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.packages " +
           "LEFT JOIN FETCH p.versions " +
           "LEFT JOIN FETCH p.reviews r " +
           "LEFT JOIN FETCH r.user " +
           "WHERE p.productId = :id")
    Optional<Products> findByIdWithDetails(@Param("id") int id);

    @Query("SELECT p FROM Products p WHERE p.status = 'approved' AND (:categoryId IS NULL OR p.category.id = :categoryId) AND (:name IS NULL OR p.name LIKE %:name%)")
    Page<Products> findApprovedByCategoryIdAndName(@Param("categoryId") Integer categoryId, @Param("name") String name, Pageable pageable);
}
