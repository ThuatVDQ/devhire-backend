package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    @Query("SELECT us FROM UserSkill us WHERE us.user.id = ?1")
    List<UserSkill> findByUserId(Long userId);

    @Query("SELECT us FROM UserSkill us WHERE us.skill.id = ?1")
    List<UserSkill> findBySkillId(Long skillId);

    @Query("SELECT us FROM UserSkill us WHERE us.user.id = ?1 AND us.skill.id = ?2")
    Optional<UserSkill> findByUserIdAndSkillId(Long userId, Long skillId);
}
