package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Licenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicensesRepository extends JpaRepository<Licenses, Integer> {
}
