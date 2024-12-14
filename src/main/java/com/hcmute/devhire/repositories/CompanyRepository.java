package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT DISTINCT c FROM Company c " +
            "JOIN c.jobs j " +
            "JOIN j.jobSkills js " +
            "JOIN js.skill s " +
            "WHERE s.id IN (" +
            "   SELECT DISTINCT s2.id FROM Job j2 " +
            "   JOIN j2.jobSkills js2 " +
            "   JOIN js2.skill s2 " +
            "   WHERE j2.company.id = :companyId" +
            ") " +
            "AND c.id != :companyId")
    List<Company> getRelatedCompanies(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT c FROM Company c ORDER BY RAND()")
    List<Company> findRandomCompanies(Pageable pageable);
}
