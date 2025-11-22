package com.homihq.db2rest.multidb;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Component
@ConfigurationProperties(prefix = "app")
@Slf4j
public class DatabaseProperties {


    private List<DatabaseConnectionDetail> databases;

    public Optional<DatabaseConnectionDetail> getDatabase(String dbId) {

        return databases.stream()
                .filter(databaseConnectionDetail -> StringUtils.equalsIgnoreCase(dbId, databaseConnectionDetail.id()))
                .findFirst();
    }

    public boolean isRdbmsConfigured() {

        if (Objects.isNull(databases)) {
            log.info("No database configuration found");
            return false;
        }

        log.info("Database configuration found.");

        
        System.out.println("Size: " + databases.size());
        for(DatabaseConnectionDetail dbConnDetal :databases)
        {
            System.out.println("And id.:" + dbConnDetal.id());
            System.out.println(dbConnDetal.url());
            System.out.println(dbConnDetal.password());
            System.out.println(dbConnDetal.type());
            System.out.println(dbConnDetal.isAutoCommit());
        }
        
        boolean jdbcUrlFound = databases.stream()
                .anyMatch(DatabaseConnectionDetail::isJdbcPresent);

        log.info("JDBC Url found : {}", jdbcUrlFound);

        return jdbcUrlFound;
    }
}
