package com.blps.app.domain.service;

import com.blps.app.domain.entity.CourseBlock;
import com.blps.app.domain.entity.LearningTask;
import com.blps.app.domain.repo.CourseBlockRepository;
import com.blps.app.domain.repo.LearningTaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner seedData(CourseBlockRepository courseBlockRepository, LearningTaskRepository learningTaskRepository) {
        return args -> {
            if (courseBlockRepository.count() > 0) {
                return;
            }

            CourseBlock block1 = courseBlockRepository.save(new CourseBlock("BLK-1", "Основы", 0, 1));
            CourseBlock block2 = courseBlockRepository.save(new CourseBlock("BLK-2", "Практика", 120, 2));

            learningTaskRepository.save(new LearningTask("T-1", "Тест по теории", 50, false, block1));
            learningTaskRepository.save(new LearningTask("T-2", "Проект с проверкой", 120, true, block1));
            learningTaskRepository.save(new LearningTask("T-3", "Финальное задание", 180, true, block2));
        };
    }
}
