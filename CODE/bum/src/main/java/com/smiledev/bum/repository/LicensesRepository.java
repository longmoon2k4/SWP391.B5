package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Licenses;
import com.smiledev.bum.entity.Users;
import com.smiledev.bum.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicensesRepository extends JpaRepository<Licenses, Integer> {

    Optional<Licenses> findByLicenseKey(String licenseKey);

    List<Licenses> findByOrder_OrderId(int orderId);

    List<Licenses> findByUser(Users user);

    List<Licenses> findTop5ByUserOrderByCreatedAtDesc(Users user);

    long countByUser(Users user);

    long countByUserAndStatus(Users user, Licenses.Status status);

    Licenses findTopByOrderByLicenseIdDesc();

    long countByStatus(Licenses.Status status);

    @Query("SELECT COUNT(l) FROM Licenses l WHERE l.product.developer = :developer")
    long countByProductDeveloper(@Param("developer") Users developer);

    @Query("SELECT COUNT(l) FROM Licenses l WHERE l.product.developer = :developer AND l.status = :status")
    long countByProductDeveloperAndStatus(@Param("developer") Users developer, @Param("status") Licenses.Status status);

    boolean existsByUserAndProduct(Users user, Products product);

    Page<Licenses> findByUserAndProduct_NameContainingIgnoreCase(Users user, String productName, Pageable pageable);
    Page<Licenses> findByUser(Users user, Pageable pageable);
}
