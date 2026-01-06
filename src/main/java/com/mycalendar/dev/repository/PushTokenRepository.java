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
 * Repository สำหรับจัดการ Push Token
 */
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    
    /**
     * หา push tokens ที่ active ทั้งหมดของ user
     */
    List<PushToken> findByUserIdAndActiveTrue(Long userId);
    
    /**
     * หา push token ตาม token string
     */
    Optional<PushToken> findByToken(String token);
    
    /**
     * ลบ token ตาม token string
     */
    @Modifying
    @Transactional
    void deleteByToken(String token);
    
    /**
     * ตรวจสอบว่า token นี้มีอยู่แล้วหรือไม่
     */
    boolean existsByToken(String token);
    
    /**
     * หา tokens ทั้งหมดของ user ตาม userId list
     */
    @Query("SELECT pt FROM PushToken pt WHERE pt.userId IN :userIds AND pt.active = true")
    List<PushToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);
}
