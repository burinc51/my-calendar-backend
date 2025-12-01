package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.UserSocialProvider;
import com.mycalendar.dev.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSocialProviderRepository extends JpaRepository<UserSocialProvider, Long> {

    @Query("""
            SELECT usp
            FROM UserSocialProvider usp
            WHERE usp.provider = :provider
              AND usp.providerId = :providerId
            """)
    Optional<UserSocialProvider> findByProviderAndProviderId(@Param("provider") ProviderType provider,
                                                             @Param("providerId") String providerId);
}
