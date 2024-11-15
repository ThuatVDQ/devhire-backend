package com.hcmute.devhire.repositories.specification;

import com.hcmute.devhire.entities.Company;
import org.springframework.data.jpa.domain.Specification;

public class CompanySpecifications {
    public static Specification<Company> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + keyword + "%");
    }
}
