package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.FavoriteJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteJobRepository extends JpaRepository<FavoriteJob, Long> {
}
