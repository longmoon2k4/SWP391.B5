package com.smiledev.bum.repository;

import com.smiledev.bum.entity.ApiKeys;
import com.smiledev.bum.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeysRepository extends JpaRepository<ApiKeys, Integer> {
    Optional<ApiKeys> findByApiKey(String apiKey);

    List<ApiKeys> findByDeveloper(Users developer);

    Page<ApiKeys> findByDeveloper(Users developer, Pageable pageable);

    long countByDeveloper(Users developer);

    long countByDeveloperAndStatus(Users developer, ApiKeys.Status status);
}
