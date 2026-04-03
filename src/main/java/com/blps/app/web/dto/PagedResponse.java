package com.blps.app.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(
        int page,
        int size,
        int totalPages,
        long totalItems,
        boolean hasNext,
        boolean hasPrevious,
        List<T> data
) {

    public static <T> PagedResponse<T> from(Page<?> page, List<T> data) {
        return new PagedResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious(),
                data
        );
    }
}
