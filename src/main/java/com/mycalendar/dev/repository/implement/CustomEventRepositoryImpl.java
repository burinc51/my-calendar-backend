package com.mycalendar.dev.repository.implement;

import com.mycalendar.dev.mapper.EventMapper;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.repository.CustomEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

import static com.mycalendar.dev.util.SqlFileLoader.loadSqlFromFile;

@Repository
public class CustomEventRepositoryImpl implements CustomEventRepository {

    private final EntityManager entityManager;

    public CustomEventRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional()
    public Page<EventResponse> findAllEventByGroup(Long groupId, Pageable pageable) {
        try {
            // Load SQL without LIMIT/OFFSET
            String baseSql = loadSqlFromFile("/db/migration/sql/event/SELECT_EVENT_BY_GROUP.sql");
            String countSql = loadSqlFromFile("/db/migration/sql/event/count/SELECT_EVENT_BY_GROUP_COUNT.sql");

            // Main query (no pagination at DB level)
            Query query = entityManager.createNativeQuery(baseSql);
            query.setParameter("groupId", groupId);

            // Count query
            Query countQuery = entityManager.createNativeQuery(countSql);
            countQuery.setParameter("groupId", groupId);

            List<Object[]> rows = query.getResultList();
            List<EventResponse> responses = EventMapper.mapRowsMerged(rows);

            // Apply pagination in-memory after mapping
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), responses.size());
            List<EventResponse> pageContent = responses.subList(start, end);

            long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(pageContent, pageable, total);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load SQL file", e);
        }
    }

}
