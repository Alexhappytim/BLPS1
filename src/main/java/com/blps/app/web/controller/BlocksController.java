package com.blps.app.web.controller;

import com.blps.app.application.service.BlockOpenResult;
import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.domain.model.CourseBlock;
import com.blps.app.web.dto.BlockDto;
import com.blps.app.web.dto.BlockUpsertRequest;
import com.blps.app.web.dto.OpenBlockRequest;
import com.blps.app.web.dto.OpenBlockResponse;
import com.blps.app.web.dto.ResolveCoefficientRequest;
import com.blps.app.web.dto.CoefficientResponse;
import com.blps.app.web.dto.PagedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blocks")
@Validated
public class BlocksController {

    private final AuthenticationService authenticationService;
    private final LearningPlatformService learningPlatformService;

    public BlocksController(AuthenticationService authenticationService, LearningPlatformService learningPlatformService) {
        this.authenticationService = authenticationService;
        this.learningPlatformService = learningPlatformService;
    }

    @GetMapping
    public PagedResponse<BlockDto> blocks(
            @RequestParam @Positive Long courseId,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        var resultPage = learningPlatformService.blocks(courseId, page);
        var data = resultPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return PagedResponse.from(resultPage, data);
    }

    @GetMapping("/{id}")
    public BlockDto blockById(@PathVariable @Positive Long id) {
        return toDto(learningPlatformService.blockById(id));
    }

    @PostMapping
    public BlockDto createBlock(@Valid @RequestBody BlockUpsertRequest request) {
        CourseBlock created = learningPlatformService.createBlock(
                request.courseId(),
                request.code(),
                request.title(),
                request.openCost(),
                request.orderIndex()
        );
        return toDto(created);
    }

    @PutMapping("/{id}")
    public BlockDto updateBlock(@PathVariable @Positive Long id, @Valid @RequestBody BlockUpsertRequest request) {
        CourseBlock updated = learningPlatformService.updateBlock(
                id,
                request.courseId(),
                request.code(),
                request.title(),
                request.openCost(),
                request.orderIndex()
        );
        return toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteBlock(@PathVariable @Positive Long id) {
        learningPlatformService.deleteBlock(id);
    }

    @PostMapping("/open")
    public OpenBlockResponse openBlock(@Valid @RequestBody OpenBlockRequest request) {
        authenticationService.authenticateByLogin(request.login());
        BlockOpenResult result = learningPlatformService.openBlock(request.login(), request.courseId(), request.blockId());
        return new OpenBlockResponse(result.blockId(), result.alreadyOpened(), result.remainingPoints());
    }

    @PostMapping("/resolve-coefficient")
    public CoefficientResponse resolveCoefficient(@Valid @RequestBody ResolveCoefficientRequest request) {
        authenticationService.authenticateByLogin(request.login());
        double coefficient = learningPlatformService.resolveCoefficient(request.difficulty());
        return new CoefficientResponse(request.difficulty(), coefficient);
    }

    private BlockDto toDto(CourseBlock block) {
        return new BlockDto(
                block.getId(),
                block.getCourse().getId(),
                block.getCode(),
                block.getTitle(),
                block.getOpenCost(),
                block.getOrderIndex()
        );
    }
}
