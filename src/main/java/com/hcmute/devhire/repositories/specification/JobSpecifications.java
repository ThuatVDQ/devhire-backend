package com.hcmute.devhire.repositories.specification;

import com.hcmute.devhire.DTOs.JobFilterDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.utils.Currency;
import com.hcmute.devhire.utils.JobStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobSpecifications {
    public static Specification<Job> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            Join<Job, JobSkill> jobSkills = root.join("jobSkills");
            Join<JobSkill, Skill> skill = jobSkills.join("skill");

            Predicate titleCondition = criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
            Predicate skillCondition = criteriaBuilder.like(skill.get("name"), "%" + keyword + "%");

            return criteriaBuilder.or(titleCondition, skillCondition);
        };
    }

    public static Specification<Job> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> jobAddressJoin = root.join("jobAddresses", JoinType.INNER);
            Join<Object, Object> addressJoin = jobAddressJoin.join("address", JoinType.INNER);
            return criteriaBuilder.equal(addressJoin.get("city"), location);
        };
    }

    public static Specification<Job> hasJobType(String jobType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), jobType);
    }

    public static Specification<Job> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Job> hasStatuses(List<JobStatus> statuses) {
        return (root, query, criteriaBuilder) -> root.get("status").in(statuses);
    }

    public static Specification<Job> hasSameSkills(Long jobId) {
        return (root, query, criteriaBuilder) -> {
            Join<Job, JobSkill> jobSkills = root.join("jobSkills");

            Join<JobSkill, Skill> skill = jobSkills.join("skill");

            Predicate jobCondition = criteriaBuilder.equal(jobSkills.get("job").get("id"), jobId);

            Predicate excludeCurrentJob = criteriaBuilder.notEqual(root.get("id"), jobId);

            Predicate condition = criteriaBuilder.and(jobCondition, excludeCurrentJob);

            return condition;
        };
    }

    public static Specification<Job> withCriteria(JobFilterDTO criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Reuse joins
            Join<Job, JobAddress> jobAddressJoin = root.join("jobAddresses", JoinType.LEFT);
            Join<JobAddress, Address> addressJoin = jobAddressJoin.join("address", JoinType.LEFT);
            Join<Job, JobSkill> jobSkillJoin = root.join("jobSkills", JoinType.LEFT);
            Join<JobSkill, Skill> skillJoin = jobSkillJoin.join("skill", JoinType.LEFT);
            Join<Job, Company> companyJoin = root.join("company", JoinType.LEFT);
            Join<Job, Category> categoryJoin = root.join("category", JoinType.LEFT);

            // Filter by city
            if (criteria.getCities() != null && !criteria.getCities().isEmpty()) {
                predicates.add(addressJoin.get("city").in(criteria.getCities()));
            }

            // Filter by district
            if (criteria.getDistricts() != null && !criteria.getDistricts().isEmpty()) {
                predicates.add(addressJoin.get("district").in(criteria.getDistricts()));
            }

            // Salary range
            if (criteria.getSalaryMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryStart"), criteria.getSalaryMin()));
            }
            if (criteria.getSalaryMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryEnd"), criteria.getSalaryMax()));
            }

            // Currency
            if (criteria.getCurrency() != null && !criteria.getCurrency().isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("currency"), Currency.valueOf(criteria.getCurrency())));
                } catch (IllegalArgumentException e) {
                    // Skip invalid enum value
                }
            }

            // Job types
            if (criteria.getTypes() != null && !criteria.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(criteria.getTypes()));
            }

            // Levels
            if (criteria.getLevels() != null && !criteria.getLevels().isEmpty()) {
                predicates.add(root.get("level").in(criteria.getLevels()));
            }

            // Experience
            if (criteria.getExperiences() != null && !criteria.getExperiences().isEmpty()) {
                predicates.add(root.get("experience").in(criteria.getExperiences()));
            }

            // Position
            if (criteria.getPositions() != null && !criteria.getPositions().isEmpty()) {
                predicates.add(root.get("position").in(criteria.getPositions()));
            }

            // Categories
            if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
                predicates.add(categoryJoin.get("name").in(criteria.getCategories()));
            }

            // Skills
            if (criteria.getSkills() != null && !criteria.getSkills().isEmpty()) {
                predicates.add(skillJoin.get("name").in(criteria.getSkills()));
            }

            // Company name (partial match, ignore case)
            if (criteria.getCompanyName() != null && !criteria.getCompanyName().isEmpty()) {
                predicates.add(cb.like(cb.lower(companyJoin.get("name")), "%" + criteria.getCompanyName().toLowerCase() + "%"));
            }

            // Only status OPEN or HOT
            predicates.add(root.get("status").in(JobStatus.HOT, JobStatus.OPEN));

            // Avoid duplicate results
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Job> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), companyId);
    }
}
