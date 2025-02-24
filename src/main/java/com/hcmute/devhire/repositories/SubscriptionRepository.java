package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("SELECT s FROM Subscription s WHERE s.name = ?1")
    Optional<Subscription> findByName(String name);

    Page<Subscription> findAll(Pageable pageable);
}
