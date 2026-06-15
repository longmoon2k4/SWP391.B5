package com.smiledev.bum.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smiledev.bum.entity.Products;
import com.smiledev.bum.entity.Users;


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

    long countByStatus(Products.Status status);

    List<Products> findByStatus(Products.Status status, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Products p " +
           "LEFT JOIN FETCH p.developer " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.versions " +
           "LEFT JOIN FETCH p.packages " +
           "WHERE p.status = :status " +
           "ORDER BY p.createdAt DESC")
    List<Products> findByStatusWithDetails(@Param("status") Products.Status status, Pageable pageable);

    long countByDeveloper(Users developer);

    long countByDeveloperAndStatus(Users developer, Products.Status status);

    List<Products> findTop5ByDeveloperOrderByUpdatedAtDesc(Users developer);

    // Get all developer products (non-paginated) for stats calculation
    List<Products> findByDeveloper(Users developer);

    // Get all developer products with eager loaded licenses for stats
    @Query("SELECT DISTINCT p FROM Products p " +
           "LEFT JOIN FETCH p.licenses l " +
           "LEFT JOIN FETCH l.order " +
           "WHERE p.developer = :developer " +
           "ORDER BY p.productId")
    List<Products> findByDeveloperWithLicenses(@Param("developer") Users developer);

    // Developer product management queries
    Page<Products> findByDeveloper(Users developer, Pageable pageable);

    Page<Products> findByDeveloperAndStatus(Users developer, Products.Status status, Pageable pageable);

    Page<Products> findByDeveloperAndNameContainingIgnoreCase(Users developer, String name, Pageable pageable);
}
