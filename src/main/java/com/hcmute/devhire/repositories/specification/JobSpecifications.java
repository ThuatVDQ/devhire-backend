package com.hcmute.devhire.repositories.specification;

import com.hcmute.devhire.entities.Job;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

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
}
