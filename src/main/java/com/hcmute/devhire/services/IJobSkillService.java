package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobSkillDTO;
import com.hcmute.devhire.entities.JobSkill;

import java.util.List;

public interface IJobSkillService {
    List<JobSkill> createJobSkills (List<JobSkillDTO> jobSkillDTOS);
}
