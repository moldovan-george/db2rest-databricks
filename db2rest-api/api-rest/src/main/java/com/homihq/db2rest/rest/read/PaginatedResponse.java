package com.homihq.db2rest.rest.read;

import java.util.List;
import java.util.Map;

public record PaginatedResponse(
        List<Map<String, Object>> data,
        PaginationMetadata pagination
) {
}
