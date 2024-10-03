package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
