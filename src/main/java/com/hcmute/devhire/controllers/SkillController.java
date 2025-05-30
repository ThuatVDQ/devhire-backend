package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.SkillDTO;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.responses.ResponseObject;
import com.hcmute.devhire.services.ISkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/skills")
public class SkillController {
    private final ISkillService skillService;
    @GetMapping("/trendingSkills")
    public ResponseEntity<?> getTrendingSkills() {
        List<SkillDTO> trendingSkills = skillService.getTrendingSkills();
        if (trendingSkills.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trendingSkills);
    }
}
