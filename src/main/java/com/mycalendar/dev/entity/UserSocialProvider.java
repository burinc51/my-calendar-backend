package com.mycalendar.dev.entity;


import com.mycalendar.dev.enums.ProviderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_social_provider",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
@Getter
@Setter
public class UserSocialProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;      // Google = sub, Facebook = facebook user id

    private String email;
    private String displayName;
    private String pictureUrl;

}