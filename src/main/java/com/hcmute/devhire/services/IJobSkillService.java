package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobSkillDTO;
import com.hcmute.devhire.entities.Skill;

import java.util.List;

public interface IJobSkillService {
    List<Skill> createJobSkills (List<JobSkillDTO> jobSkillDTOS);
}
