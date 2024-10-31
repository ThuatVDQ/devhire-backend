package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteJobRepository extends JpaRepository<FavoriteJob, Long> {
    FavoriteJob findByUserAndJob(User user, Job job);

    List<FavoriteJob> findByUser(User user);
}
