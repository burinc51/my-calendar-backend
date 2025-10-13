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
            // โหลด SQL จากไฟล์
            String baseSql = loadSqlFromFile("/db/migration/sql/event/SELECT_EVENT_BY_GROUP.sql");
            String countSql = loadSqlFromFile("/db/migration/sql/event/count/SELECT_EVENT_BY_GROUP_COUNT.sql");

            // Query หลัก (data)
            Query query = entityManager.createNativeQuery(baseSql);
            query.setParameter("groupId", groupId);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            // Query นับจำนวนทั้งหมด
            Query countQuery = entityManager.createNativeQuery(countSql);
            countQuery.setParameter("groupId", groupId);

            List<Object[]> rows = query.getResultList();
            List<EventResponse> responses = EventMapper.mapRowsMerged(rows);


            long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(responses, pageable, total);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load SQL file", e);
        }
    }

}
