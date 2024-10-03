package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.JobSkill;
import com.hcmute.devhire.DTOs.JobSkillDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobSkillService implements IJobSkillService {
    public List<JobSkill> createJobSkills(List<JobSkillDTO> jobSkillDTOs) {
        return jobSkillDTOs.stream()
                .map(skillDTO -> JobSkill.builder()
                        .name(skillDTO.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
