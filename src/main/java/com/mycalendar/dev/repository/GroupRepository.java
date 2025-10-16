package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.projection.GroupProjection;
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
            SELECT
                g.groupId               AS groupId,
                g.groupName             AS groupName,
                g.description           AS description,
                m.userId                AS userId,
                m.username              AS username,
                m.name                  AS name,
                p.permissionName        AS permissionName
            FROM UserGroup ug
                JOIN ug.group g
                JOIN g.userGroups ugm
                JOIN ugm.user m
                JOIN ugm.permission p
            WHERE ug.user.userId = :userId
            ORDER BY g.groupId, m.userId
            """)
    List<GroupProjection> findAllGroupsByUserId(@Param("userId") Long userId);
}
