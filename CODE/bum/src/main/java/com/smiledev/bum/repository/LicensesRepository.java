package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Licenses;
import com.smiledev.bum.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicensesRepository extends JpaRepository<Licenses, Integer> {

    List<Licenses> findByOrder_OrderId(int orderId);

    List<Licenses> findByUser(Users user);

    Licenses findTopByOrderByLicenseIdDesc();

    long countByStatus(Licenses.Status status);

    @Query("SELECT COUNT(l) FROM Licenses l WHERE l.product.developer = :developer")
    long countByProductDeveloper(@Param("developer") Users developer);

    @Query("SELECT COUNT(l) FROM Licenses l WHERE l.product.developer = :developer AND l.status = :status")
    long countByProductDeveloperAndStatus(@Param("developer") Users developer, @Param("status") Licenses.Status status);
}
