package com.blps.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false)
    private long points;

    protected AppUser() {
    }

    public AppUser(String login) {
        this.login = login;
        this.points = 0;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public long getPoints() {
        return points;
    }

    public void addPoints(long delta) {
        this.points += delta;
    }

    public void subtractPoints(long delta) {
        this.points -= delta;
    }
}
