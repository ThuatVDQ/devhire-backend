package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Skill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Skill findSkillByName(String name);
    @Query("SELECT s.name FROM Skill s")
    List<String> findAllSkillNames();

    @Query(value = "SELECT * FROM skill ORDER BY frequency DESC LIMIT 8", nativeQuery = true)
    List<Skill> findTop8TrendingSkills();
}
