package com.mycalendar.dev.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericSpecification<T> {
    public Specification<T> getSpecification(String keyword, List<String> fields) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (keyword == null || fields.isEmpty()) return null;
            String likePattern = "%" + keyword.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field).as(String.class)), likePattern));
            }
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<T> getSpecification(Map<String, Object> keywordMap, List<String> fields) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (keywordMap == null || keywordMap.isEmpty() || fields.isEmpty()) return null;

            List<Predicate> predicates = new ArrayList<>();

            for (String field : fields) {
                if (keywordMap.containsKey(field)) {
                    Object keywordObj = keywordMap.get(field);
                    String keyword = (keywordObj != null) ? keywordObj.toString() : null;

                    if (keyword != null && !keyword.isEmpty()) {
                        String likePattern = "%" + keyword.toLowerCase() + "%";
                        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(field).as(String.class)), likePattern));
                    }
                }
            }

            return predicates.isEmpty() ? null : criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
