package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Licenses;
import com.smiledev.bum.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicensesRepository extends JpaRepository<Licenses, Integer> {

    List<Licenses> findByOrder_OrderId(int orderId);

    List<Licenses> findByUser(Users user);

    Licenses findTopByOrderByLicenseIdDesc();

    long countByStatus(Licenses.Status status);
}
