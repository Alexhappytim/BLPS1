package com.blps.app.web.controller;

import com.blps.app.application.service.BlockOpenResult;
import com.blps.app.application.service.LearningPlatformService;
import com.blps.app.auth.AuthenticationService;
import com.blps.app.web.dto.BlockDto;
import com.blps.app.web.dto.OpenBlockRequest;
import com.blps.app.web.dto.OpenBlockResponse;
import com.blps.app.web.dto.ResolveCoefficientRequest;
import com.blps.app.web.dto.CoefficientResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<BlockDto> blocks(@RequestParam @Positive Long courseId) {
        return learningPlatformService.blocks(courseId).stream()
                .map(block -> new BlockDto(
                        block.getId(),
                        block.getCourse().getId(),
                        block.getCode(),
                        block.getTitle(),
                        block.getOpenCost(),
                        block.getOrderIndex()
                ))
                .toList();
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
}
