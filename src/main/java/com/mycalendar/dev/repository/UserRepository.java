package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String token);

    @Query(value = "select * from users where activate_code = :activateCode", nativeQuery = true)
    Optional<User> findByActivateCode(@Param("activateCode") String activateCode);
}
