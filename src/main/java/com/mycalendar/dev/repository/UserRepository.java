package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    @Query(value = "SELECT * FROM users WHERE username = :username LIMIT 1", nativeQuery = true)
    Optional<User> findByUsernameIncludingInactive(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailIncludingInactive(@Param("email") String email);

    Optional<User> findByResetPasswordToken(String token);

    @Query(value = "SELECT * FROM users WHERE email = :email AND otp_code = :otpCode LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailAndOtpCodeIncludingInactive(@Param("email") String email, @Param("otpCode") String otpCode);

    @Query("SELECT u.userId FROM User u WHERE u.username = :username")
    Long findIdByUsername(@Param("username") String username);

    @Query("""
            SELECT DISTINCT u
            FROM User u
                JOIN u.roles r
            WHERE r.name = :roleName
              AND u.userId <> :requestUserId
            ORDER BY u.name ASC, u.userId ASC
            """)
    List<User> findUsersByRoleNameExcludingUser(@Param("roleName") String roleName,
                                                @Param("requestUserId") Long requestUserId);

    @Query("""
            SELECT DISTINCT u
            FROM User u
                JOIN u.roles r
            WHERE r.name = :roleName
              AND u.userId <> :requestUserId
            """)
    Page<User> findUsersByRoleNameExcludingUser(@Param("roleName") String roleName,
                                                @Param("requestUserId") Long requestUserId,
                                                Pageable pageable);

    @Query("""
            SELECT DISTINCT u
            FROM User u
                JOIN u.roles r
            WHERE r.name = :roleName
              AND u.userId <> :requestUserId
              AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :nameFilter, '%'))
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :nameFilter, '%')))
            """)
    Page<User> findUsersByRoleNameExcludingUserWithNameFilter(
            @Param("roleName") String roleName,
            @Param("requestUserId") Long requestUserId,
            @Param("nameFilter") String nameFilter,
            Pageable pageable);
}
