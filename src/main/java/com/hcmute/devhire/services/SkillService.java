package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.SkillDTO;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillService implements ISkillService {
    private final SkillRepository skillRepository;

    @Override
    public Skill createSkill(SkillDTO skillDTO) {
        Skill skill = Skill.builder()
                .name(skillDTO.getName())
                .build();
        return skillRepository.save(skill);
    }
}
