package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.phone = ?1")
    boolean existByPhoneNumber(String phone);
    Optional<User> findByPhone(String phone);
}
