package com.hcmute.devhire.repositories.specification;

import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobSkill;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.utils.JobStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
            // Tạo subquery để lấy tất cả các jobId có kỹ năng chung với jobId đã cho
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<JobSkill> jobSkillSubqueryRoot = subquery.from(JobSkill.class);

            // Lấy kỹ năng của công việc có jobId đã cho
            subquery.select(jobSkillSubqueryRoot.get("job").get("id"))
                    .where(criteriaBuilder.equal(jobSkillSubqueryRoot.get("job").get("id"), jobId));

            // So sánh công việc chính (root) với kết quả từ subquery để tìm các công việc có chung kỹ năng
            return criteriaBuilder.in(root.get("id")).value(subquery);
        };
    }


    public static Specification<Job> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), companyId);
    }
}
