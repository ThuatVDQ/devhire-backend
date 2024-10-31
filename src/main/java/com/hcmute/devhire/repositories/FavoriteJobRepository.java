package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteJobRepository extends JpaRepository<FavoriteJob, Long> {
    FavoriteJob findByUserAndJob(User user, Job job);
}
