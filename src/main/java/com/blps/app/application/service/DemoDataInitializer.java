package com.blps.app.application.service;

import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.CourseBlock;
import com.blps.app.domain.model.LearningTask;
import com.blps.app.domain.model.ReviewType;
import com.blps.app.domain.repository.CourseRepository;
import com.blps.app.domain.repository.CourseBlockRepository;
import com.blps.app.domain.repository.LearningTaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner seedData(CourseRepository courseRepository,
                               CourseBlockRepository courseBlockRepository,
                               LearningTaskRepository learningTaskRepository) {
        return args -> {
            Course frontendCourse = ensureCourse(courseRepository, "FRONTEND", "Frontend Developer");
            Course backendCourse = ensureCourse(courseRepository, "BACKEND", "Backend Developer");

            CourseBlock feBlock1 = ensureBlock(courseBlockRepository, "FE-BLK-1", "HTML/CSS", 0, 1, frontendCourse);
            CourseBlock feBlock2 = ensureBlock(courseBlockRepository, "FE-BLK-2", "JavaScript", 120, 2, frontendCourse);
            CourseBlock beBlock1 = ensureBlock(courseBlockRepository, "BE-BLK-1", "Java Core", 0, 1, backendCourse);
            CourseBlock beBlock2 = ensureBlock(courseBlockRepository, "BE-BLK-2", "Spring", 130, 2, backendCourse);

            ensureTask(learningTaskRepository, "FE-T-1", "Тест по теории", 50, ReviewType.AUTO, feBlock1);
            ensureTask(learningTaskRepository, "FE-T-2", "Проект с проверкой", 120, ReviewType.MENTOR, feBlock1);
            ensureTask(learningTaskRepository, "FE-T-3", "SPA модуль", 180, ReviewType.MENTOR, feBlock2);

            ensureTask(learningTaskRepository, "BE-T-1", "Java quiz", 60, ReviewType.AUTO, beBlock1);
            ensureTask(learningTaskRepository, "BE-T-2", "REST сервис", 140, ReviewType.MENTOR, beBlock2);
        };
    }

    private Course ensureCourse(CourseRepository courseRepository, String code, String title) {
        return courseRepository.findByCode(code)
                .orElseGet(() -> courseRepository.save(new Course(code, title)));
    }

    private CourseBlock ensureBlock(CourseBlockRepository courseBlockRepository,
                                    String code,
                                    String title,
                                    long openCost,
                                    int orderIndex,
                                    Course course) {
        return courseBlockRepository.findByCode(code)
                .orElseGet(() -> courseBlockRepository.save(new CourseBlock(code, title, openCost, orderIndex, course)));
    }

    private LearningTask ensureTask(LearningTaskRepository learningTaskRepository,
                                    String code,
                                    String title,
                                    long basePoints,
                        ReviewType reviewType,
                                    CourseBlock block) {
        return learningTaskRepository.findByCode(code)
                .orElseGet(() -> learningTaskRepository.save(
                new LearningTask(code, title, basePoints, reviewType, block)
                ));
    }
}
