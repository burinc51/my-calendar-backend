package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findAll(Specification<Group> spec, Pageable pageable);

    Page<Group> findAllByCreatorId(Long creatorId, Pageable pageable);

    @Query("SELECT DISTINCT g " +
            "FROM Group g " +
            "JOIN g.members gm " +
            "WHERE gm.user.id = :userId")
    Page<Group> findAllByMemberUserId(@Param("userId") Long userId, Pageable pageable);
}