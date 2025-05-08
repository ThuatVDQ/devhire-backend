package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.JobSkill;
import com.hcmute.devhire.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    void deleteAllByJobId(Long jobId);
    @Query("SELECT s.name FROM JobSkill js JOIN Skill s ON js.skill.id = s.id WHERE js.job.id = ?1")
    List<String> findAllSkillNameByJobId(Long jobId);
}
