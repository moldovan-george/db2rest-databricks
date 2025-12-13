This project is a fork of DB2Rest. It extends functionality by adding support for querying Databricks Unity Catalog tables through connections to SQL Warehouses.

# Important
Edit the application.yaml file, as per the following example:
-  use *databricks* for the *id*
- set the optional *autoCommit* to *true*
- the *schema* name stands actually for the catalog name

# Authorization
Authentication is performed in currently through PATs only.

## Key Notes
- Metadata loading happens once at startup and whenever `JdbcManager.reload()` is invoked, ensuring schema/table caches stay fresh.
- Template-based SQL generation lets the same REST layer target multiple dialects without hardcoding vendor-specific syntax.
- By using `RoutingDataSource` + request-scoped dbId resolution, multi-tenancy stays transparent to controllers and services.

# TODO List for the first release
- [ ] Add pagination support
- [x] Support hive_metastore alongside UC catalogs
- [ ] Confirm OAuth authorization works as expected

# Websites
[https://db2rest.com](https://db2rest.com)

[https://databricks.com](https://databricks.com)

# Latest Release
None yet

# References
Check the linked projects for more details.

# Service Workflow High-Level
## Summary
- `application.yml` defines databases under `app.databases`. Spring Boot binds that block into `DatabaseProperties`, giving `DatabaseConnectionDetail` objects with JDBC info and schema/catalog filters.
- `DbServiceConfiguration` builds a `RoutingDataSource` (HikariCP pools) from those details, wires `JdbcManager` with all SQL dialects, and configures the JTE template engine plus JDBC services.
- On startup `JdbcManager.reload()` grabs metadata for each configured data source through `JdbcMetaDataProvider`, chooses the matching dialect, and caches `NamedParameterJdbcTemplate` + `TransactionTemplate` per database.
- `DatabaseContextRequestInterceptor` inspects each HTTP request (headers, query params, or auth context) to decide which `dbId` is active and stores it so downstream services resolve the right tenant.
- REST controllers (e.g., `ReadController`) delegate to service classes (`JdbcReadService`, etc.). Those services chain processors to shape the request, then call `SqlCreatorTemplate` to render the proper SQL via precompiled `read.jte` (or other templates).
- `SqlCreatorTemplate` looks up metadata from `JdbcManager`, feeds the model into the template engine, and hands the SQL/params to `JdbcOperationService`, which uses the cached `NamedParameterJdbcTemplate` to execute against the selected pool.
- Result sets are mapped to DTOs or generic maps, post-processed, and returned as JSON responses by Spring MVC.

## ASCII Request Flow
```
application.yml
      |
      v
DatabaseProperties  --> DbServiceConfiguration --> RoutingDataSource/Hikari pools
      |                                       \
      |                                        --> JTE TemplateEngine
      v
  JdbcManager (metadata + dialect cache)
      |
HTTP Request ---> DatabaseContextRequestInterceptor (resolve dbId)
      |
      v
  REST Controller (e.g., ReadController)
      |
      v
  JDBC Service (JdbcReadService + processors)
      |
      v
  SqlCreatorTemplate --(uses JTE + DbMeta)--> SQL string + params
      |
      v
NamedParameterJdbcTemplate (from JdbcManager) -- JDBC driver --> RDBMS
      |
      v
 Result mapping --> JSON response
```
