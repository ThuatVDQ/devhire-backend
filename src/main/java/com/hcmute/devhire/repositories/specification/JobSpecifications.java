package com.hcmute.devhire.repositories.specification;

import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobSkill;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.utils.JobStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class JobSpecifications {
    public static Specification<Job> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
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



    public static Specification<Job> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), companyId);
    }
}
