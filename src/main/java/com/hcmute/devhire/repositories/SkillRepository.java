package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Skill findSkillByName(String name);
    @Query("SELECT s.name FROM Skill s")
    List<String> findAllSkillNames();
}
