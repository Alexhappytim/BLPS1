package com.blps.app.web.controller;

import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.domain.model.Course;
import com.blps.app.web.dto.CourseDto;
import com.blps.app.web.dto.CourseUpsertRequest;
import com.blps.app.web.dto.PagedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@Validated
public class CoursesController {

    private final LearningPlatformService learningPlatformService;

    public CoursesController(LearningPlatformService learningPlatformService) {
        this.learningPlatformService = learningPlatformService;
    }

    @GetMapping
    public PagedResponse<CourseDto> courses(@RequestParam(defaultValue = "0") @Min(0) int page) {
        var resultPage = learningPlatformService.courses(page);
        var data = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return PagedResponse.from(resultPage, data);
    }

    @GetMapping("/{id}")
    public CourseDto courseById(@PathVariable @Positive Long id) {
        return toDto(learningPlatformService.courseById(id));
    }

    @PostMapping
    public CourseDto createCourse(@Valid @RequestBody CourseUpsertRequest request) {
        Course created = learningPlatformService.createCourse(request.code(), request.title());
        return toDto(created);
    }

    @PutMapping("/{id}")
    public CourseDto updateCourse(@PathVariable @Positive Long id, @Valid @RequestBody CourseUpsertRequest request) {
        Course updated = learningPlatformService.updateCourse(id, request.code(), request.title());
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable @Positive Long id) {
        learningPlatformService.deleteCourse(id);
    }

    private CourseDto toDto(Course course) {
        return new CourseDto(course.getId(), course.getCode(), course.getTitle());
    }
}
