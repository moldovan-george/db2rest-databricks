package com.db2rest.jdbc.dialect.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Database {

    ORACLE("Oracle"),
    MSSQL("Microsoft SQL Server"),
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    MARIADB("MariaDB"),
    SQLITE("SQLite"),
    DB2("DB2/UDB"),
    DATABIRCKS("SparkSQL");

    private final String productName;

}
