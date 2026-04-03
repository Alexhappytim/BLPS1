package com.blps.app.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private AppUserRole role;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "enabled")
    private Boolean enabled;

    protected AppUser() {
    }

    public AppUser(String login) {
        this.login = login;
        this.role = AppUserRole.USER;
        this.emailVerified = false;
        this.enabled = false;
    }

    public AppUser(String login, String passwordHash, AppUserRole role, boolean emailVerified, boolean enabled) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = emailVerified;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AppUserRole getRole() {
        return role == null ? AppUserRole.USER : role;
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public void enableAndMarkEmailVerified() {
        this.emailVerified = true;
        this.enabled = true;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(AppUserRole role) {
        this.role = role;
    }

}
