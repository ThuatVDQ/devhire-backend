package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.name = ?1")
    Category findByName(String name);
}
