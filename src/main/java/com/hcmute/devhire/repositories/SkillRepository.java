package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Skill findSkillByName(String name);
}
