package com.blps.app.web.dto;

public record BlockDto(Long id, Long courseId, String code, String title, long openCost, int orderIndex) {
}
