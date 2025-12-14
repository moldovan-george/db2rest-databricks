package com.homihq.db2rest.rest.read;

import com.homihq.db2rest.config.Db2RestConfigProperties;
import com.homihq.db2rest.jdbc.core.service.CountQueryService;
import com.homihq.db2rest.jdbc.core.service.ReadService;
import com.homihq.db2rest.jdbc.dto.JoinDetail;
import com.homihq.db2rest.jdbc.dto.ReadContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.homihq.db2rest.rest.RdbmsRestApi.VERSION;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReadController {

    private final ReadService readService;
    private final CountQueryService countQueryService;
    private final Db2RestConfigProperties db2RestConfigProperties;

    @GetMapping(value = VERSION + "/{dbId}/{tableName}", produces = "application/json")
    public PaginatedResponse findAll(
            @PathVariable String dbId,
            @PathVariable String tableName,
            @RequestHeader(name = "Accept-Profile", required = false) String schemaName,
            @RequestParam(required = false, defaultValue = "*") String fields,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(name = "sort", required = false, defaultValue = "") List<String> sorts,
            @RequestParam(required = false, defaultValue = "-1") int limit,
            @RequestParam(required = false, defaultValue = "-1") long offset) {

        log.debug("filter - {}", filter);

        ReadContext readContext = buildReadContext(dbId, schemaName, tableName, fields, filter, sorts, limit, offset, null);

        return executeRead(readContext);
    }

    @PostMapping(value = VERSION + "/{dbId}/{tableName}/_expand", produces = "application/json")
    public PaginatedResponse find(
            @PathVariable String dbId,
            @PathVariable String tableName,
            @RequestHeader(name = "Accept-Profile", required = false) String schemaName,
            @RequestParam(required = false, defaultValue = "*") String fields,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(name = "sort", required = false, defaultValue = "") List<String> sorts,
            @RequestParam(required = false, defaultValue = "-1") int limit,
            @RequestParam(required = false, defaultValue = "-1") long offset,
            @RequestBody List<JoinDetail> joins) {
        ReadContext readContext = buildReadContext(dbId, schemaName, tableName, fields, filter, sorts, limit, offset, joins);

        return executeRead(readContext);
    }

    private ReadContext buildReadContext(String dbId,
                                         String schemaName,
                                         String tableName,
                                         String fields,
                                         String filter,
                                         List<String> sorts,
                                         int limit,
                                         long offset,
                                         List<JoinDetail> joins) {
        return ReadContext.builder()
                .dbId(dbId)
                .schemaName(schemaName)
                .tableName(tableName)
                .fields(fields)
                .filter(filter)
                .sorts(sorts)
                .limit(limit)
                .defaultFetchLimit(db2RestConfigProperties.getDefaultFetchLimit())
                .offset(offset)
                .joins(joins)
                .build();
    }

    private PaginatedResponse executeRead(ReadContext readContext) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records =
                (List<Map<String, Object>>) readService.findAll(readContext);
        PaginationMetadata pagination = buildPaginationMetadata(readContext, records.size());
        return new PaginatedResponse(records, pagination);
    }

    private PaginationMetadata buildPaginationMetadata(ReadContext readContext, int currentResultSize) {
        long totalDocuments = countQueryService.count(buildCountContext(readContext)).count();
        long safeOffset = Math.max(readContext.getOffset(), 0);

        int effectivePageSize = readContext.getLimit() > -1
                ? readContext.getLimit()
                : db2RestConfigProperties.getDefaultFetchLimit();
        if (effectivePageSize <= 0) {
            effectivePageSize = currentResultSize;
        }

        int currentPage = effectivePageSize > 0 ? (int) (safeOffset / effectivePageSize) + 1 : 1;
        long remainingDocuments = Math.max(totalDocuments - (safeOffset + currentResultSize), 0);
        boolean matchesRequestedSize = effectivePageSize > 0 && currentResultSize == effectivePageSize;
        long remainingPages = matchesRequestedSize
                ? (remainingDocuments + effectivePageSize - 1L) / effectivePageSize
                : 0;

        return new PaginationMetadata(
                currentPage,
                currentResultSize,
                remainingPages,
                totalDocuments
        );
    }

    private ReadContext buildCountContext(ReadContext readContext) {
        return ReadContext.builder()
                .dbId(readContext.getDbId())
                .schemaName(readContext.getSchemaName())
                .tableName(readContext.getTableName())
                .filter(readContext.getFilter())
                .sorts(readContext.getSorts())
                .joins(readContext.getJoins())
                .defaultFetchLimit(readContext.getDefaultFetchLimit())
                .build();
    }
}
