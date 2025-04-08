package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.MemberVip;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MemberVipRepository extends JpaRepository<MemberVip, Long> {
    @Query("SELECT mv FROM MemberVip mv WHERE mv.user.id = :userId")
    List<MemberVip> findByUserId(@Param("userId") Long userId);

    Optional<MemberVip> findFirstByUserAndStatusAndSignDayBeforeAndExpireDayAfter(
            User user, Status status, Date signDay, Date expireDay
    );

    Optional<MemberVip> findTopByUserIdOrderByExpireDayDesc(Long userId);

    List<MemberVip> findByExpireDayBefore(Date date);
}
