package com.demo.auth.models.entities;

import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.models.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private static final int LOCK_DURATION_MINUTES = 30;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProviderType provider = ProviderType.LOCAL;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private int failedAttempts = 0;

    @Column
    private Instant lockTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), indexes = @Index(name = "idx_user_roles_user", columnList = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<RoleType> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();

    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ACCOUNT LOCK LOGIC

    public boolean isLocked() {

        if (!accountLocked)
            return false;

        if (lockTime != null &&
                lockTime.plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES)
                        .isBefore(Instant.now())) {

            accountLocked = false;
            failedAttempts = 0;
            lockTime = null;
        }

        return accountLocked;
    }

    public void increaseFailedAttempts() {
        this.failedAttempts++;
    }

    public void lockAccount() {
        this.accountLocked = true;
        this.lockTime = Instant.now();
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.accountLocked = false;
        this.lockTime = null;
    }
}