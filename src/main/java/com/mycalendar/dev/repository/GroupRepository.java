package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Page<Group> findAll(Pageable pageable);

    @Query("""
                SELECT DISTINCT g
                FROM Group g
                JOIN g.users u
                WHERE u.userId = :userId
            """)
    List<Group> findAllByUserId(@Param("userId") Long userId);
}
