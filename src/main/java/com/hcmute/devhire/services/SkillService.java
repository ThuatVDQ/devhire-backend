package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.SkillDTO;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService implements ISkillService {
    private final SkillRepository skillRepository;

    @Override
    public Skill createSkill(SkillDTO skillDTO) {
        Skill skill = skillRepository.findSkillByName(skillDTO.getName());
        if (skill != null) {
            return skill;
        } else {
            skill = Skill.builder()
                    .name(skillDTO.getName())
                    .build();
            return skillRepository.save(skill);
        }
    }

    @Override
    public List<SkillDTO> getTrendingSkills() {
        return skillRepository.findTop8TrendingSkills()
                .stream()
                .map(s -> new SkillDTO(s.getName(), s.getFrequency()))
                .collect(Collectors.toList());
    }

    @Override
    public void increaseSkillFrequency(Long skillId) {
        Skill skill = skillRepository.findById(skillId).orElse(null);
        if (skill != null) {
            skill.setFrequency(skill.getFrequency() + 1);
            skillRepository.save(skill);
        }
    }

    @Override
    public Skill findSkillById(Long id) {
        return skillRepository.findById(id).orElse(null);
    }

    @Override
    public Skill findSkillByName(String name) {
        return skillRepository.findSkillByName(name);
    }

}
