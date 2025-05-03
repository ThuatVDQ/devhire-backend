package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.entities.UserSkill;
import com.hcmute.devhire.repositories.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSkillService implements IUserSkillService {
    private final UserSkillRepository userSkillRepository;

    @Override
    public void add(User user, Skill skill) {
        Optional<UserSkill> userSkill = userSkillRepository.findByUserIdAndSkillId(user.getId(), skill.getId());
        if (userSkill.isEmpty()) {
            UserSkill newUserSkill = new UserSkill();
            newUserSkill.setUser(user);
            newUserSkill.setSkill(skill);
            userSkillRepository.save(newUserSkill);
        }
    }
}
