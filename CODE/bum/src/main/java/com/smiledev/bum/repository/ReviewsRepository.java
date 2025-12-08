package com.smiledev.bum.repository;

import com.smiledev.bum.entity.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Integer> {

    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(Integer productId);
}
