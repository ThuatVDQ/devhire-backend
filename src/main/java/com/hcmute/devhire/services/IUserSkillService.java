package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.entities.User;

public interface IUserSkillService {
    void add(User user, Skill skill);
}
