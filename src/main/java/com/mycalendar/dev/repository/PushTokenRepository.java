package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Push Tokens.
 */
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    /**
     * Finds all active push tokens for a given user.
     */
    List<PushToken> findByUserIdAndActiveTrue(Long userId);

    /**
     * Finds a push token by its token string.
     */
    Optional<PushToken> findByToken(String token);

    /**
     * Deletes a push token by its token string.
     */
    @Modifying
    @Transactional
    void deleteByToken(String token);

    /**
     * Checks whether a token already exists.
     */
    boolean existsByToken(String token);

    /**
     * Finds all active tokens for a list of user IDs.
     */
    @Query("SELECT pt FROM PushToken pt WHERE pt.userId IN :userIds AND pt.active = true")
    List<PushToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);
}
