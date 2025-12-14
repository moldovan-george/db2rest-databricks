package com.homihq.db2rest.rest.read;

public record PaginationMetadata(
        int currentPage,
        int pageSize,
        long remainingPages,
        long totalDocuments
) {
}
