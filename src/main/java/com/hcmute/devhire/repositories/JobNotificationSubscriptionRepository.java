package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.JobNotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobNotificationSubscriptionRepository extends JpaRepository<JobNotificationSubscription, Long> {
    Optional<JobNotificationSubscription> findByEmail(String email);
}
