package com.blps.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_block")
public class CourseBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private long openCost;

    @Column(nullable = false)
    private int orderIndex;

    protected CourseBlock() {
    }

    public CourseBlock(String code, String title, long openCost, int orderIndex) {
        this.code = code;
        this.title = title;
        this.openCost = openCost;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public long getOpenCost() {
        return openCost;
    }

    public int getOrderIndex() {
        return orderIndex;
    }
}
