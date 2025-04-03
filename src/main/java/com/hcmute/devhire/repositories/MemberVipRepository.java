package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.MemberVip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberVipRepository extends JpaRepository<MemberVip, Long> {
    @Query("SELECT mv FROM MemberVip mv WHERE mv.user.id = :userId")
    List<MemberVip> findByUserId(@Param("userId") Long userId);
}
