package com.hcmute.devhire.repositories.specification;

import com.hcmute.devhire.entities.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    public static Specification<User> hasRole(Long roleId) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> roleJoin = root.join("role");
            return criteriaBuilder.equal(roleJoin.get("id"), roleId);
        };
    }

    public static Specification<User> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

}
