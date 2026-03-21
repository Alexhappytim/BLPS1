package com.blps.app.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_block_access")
public class UserBlockAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id")
    private CourseBlock block;

    @Column(nullable = false)
    private OffsetDateTime unlockedAt;

    protected UserBlockAccess() {
    }

    public UserBlockAccess(AppUser user, CourseBlock block) {
        this.user = user;
        this.block = block;
        this.unlockedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public CourseBlock getBlock() {
        return block;
    }

    public OffsetDateTime getUnlockedAt() {
        return unlockedAt;
    }
}
