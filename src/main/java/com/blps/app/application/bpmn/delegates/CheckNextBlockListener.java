package com.blps.app.application.bpmn.delegates;

import com.blps.app.domain.model.CourseBlock;
import com.blps.app.domain.repository.CourseBlockRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("checkNextBlockListener")
public class CheckNextBlockListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(CheckNextBlockListener.class);

    private final CourseBlockRepository courseBlockRepository;

    public CheckNextBlockListener(CourseBlockRepository courseBlockRepository) {
        this.courseBlockRepository = courseBlockRepository;
    }

    @Override
    public void notify(DelegateExecution execution) {
        Long courseId = getLong(execution.getVariable("courseId"));
        Long blockId = getLong(execution.getVariable("blockId"));

        if (courseId == null) {
            execution.setVariable("hasNextBlock", false);
            return;
        }

        List<CourseBlock> blocks = courseBlockRepository.findByCourseIdWithCourse(courseId);
        if (blocks.isEmpty()) {
            execution.setVariable("hasNextBlock", false);
            return;
        }

        int currentIndex = -1;
        if (blockId != null) {
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i).getId().equals(blockId)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        // If current block is the last block of the course, hasNextBlock is false!
        boolean hasNext = currentIndex >= 0 && currentIndex < blocks.size() - 1;
        execution.setVariable("hasNextBlock", hasNext);
        log.info("CheckNextBlockListener: login={}, courseId={}, blockId={}, blockIndex={}/{}, hasNextBlock={}",
                execution.getVariable("login"), courseId, blockId, currentIndex, blocks.size(), hasNext);
    }

    private Long getLong(Object val) {
        if (val instanceof Number num) {
            return num.longValue();
        }
        if (val instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
