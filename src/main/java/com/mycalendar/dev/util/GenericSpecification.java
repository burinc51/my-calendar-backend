package com.mycalendar.dev.util;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.HashMap;
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
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (keywordMap == null || keywordMap.isEmpty() || fields.isEmpty()) return null;

            List<Predicate> predicates = new ArrayList<>();
            Map<String, Join<?, ?>> joins = new HashMap<>();

            for (String field : fields) {
                Object value = keywordMap.get(field);
                if (value == null) continue;

                String[] parts = field.split("\\.");
                Path<?> path;

                if (parts.length == 1) {
                    path = root.get(parts[0]);
                } else {
                    Join<?, ?> join = joins.computeIfAbsent(parts[0], j -> root.join(j, JoinType.LEFT));
                    path = join.get(parts[1]);
                }

                if (value instanceof String stringValue) {
                    String likePattern = "%" + stringValue.toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(path.as(String.class)), likePattern));
                } else {
                    predicates.add(cb.equal(path, value));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
