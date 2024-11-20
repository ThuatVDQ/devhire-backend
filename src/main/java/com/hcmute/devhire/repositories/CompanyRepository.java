package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {
    Page<Company> findAll(Pageable pageable);
    Optional<Company> findById(Long id);

    @Query("SELECT c FROM Company c WHERE c.createdBy.email = :username OR c.createdBy.phone = :username")
    Company findByUser(@Param("username") String username);

    @Query("SELECT COUNT(c) FROM Company c")
    int countCompanies();

    @Query("SELECT COUNT(c) FROM Company c WHERE MONTH(c.createdAt) = :month AND YEAR(c.createdAt) = :year")
    int countCompaniesMonthly(@Param("month") int month, @Param("year") int year);
}
