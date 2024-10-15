package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSkillRepository extends JpaRepository<Skill, Long> {
}
