package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.JobSkill;
import com.hcmute.devhire.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    void deleteAllByJobId(Long jobId);
}
