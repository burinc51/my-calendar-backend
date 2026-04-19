package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Report;
import com.mycalendar.dev.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    @Query("""
            SELECT r FROM Report r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:category IS NULL OR LOWER(r.category) LIKE LOWER(CONCAT('%', CAST(:category AS string), '%')))
              AND (:username IS NULL OR EXISTS (
                    SELECT u FROM User u
                    WHERE u.userId = r.userId
                      AND LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:username AS string), '%'))
                  ))
            """)
    Page<Report> findAllWithFilters(
            @Param("status") ReportStatus status,
            @Param("category") String category,
            @Param("username") String username,
            Pageable pageable
    );
}
