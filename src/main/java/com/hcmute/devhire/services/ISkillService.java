package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.SkillDTO;
import com.hcmute.devhire.entities.Skill;

public interface ISkillService {
    Skill createSkill(SkillDTO skillDTO);
    Skill findSkillById(Long id);
    Skill findSkillByName(String name);
}
