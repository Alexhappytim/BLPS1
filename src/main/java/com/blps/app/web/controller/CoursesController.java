package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.web.dto.CourseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CoursesController {

    private final LearningPlatformService learningPlatformService;

    public CoursesController(LearningPlatformService learningPlatformService) {
        this.learningPlatformService = learningPlatformService;
    }

    @GetMapping
    public List<CourseDto> courses() {
        return learningPlatformService.courses().stream()
                .map(course -> new CourseDto(course.getId(), course.getCode(), course.getTitle()))
                .toList();
    }
}
