package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.phone = ?1")
    boolean existByPhoneNumber(String phone);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.email = ?1")
    boolean existByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleAccountId(String id);
    Optional<User> findByFacebookAccountId(String id);
    @Query("SELECT COUNT(u) FROM User u")
    int countUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE MONTH(u.createdAt) = ?1 AND YEAR(u.createdAt) = ?2")
    int countUsersMonthly(int month, int year);

    List<User> findAllByRoleName(String roleName);

    List<User> findTop5ByOrderByCreatedAtDesc();

}
