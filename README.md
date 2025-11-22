This project is a fork of DB2Rest. It extends functionality by adding support for querying Databricks Unity Catalog tables through connections to SQL Warehouses.

# Important
Edit the application.yaml file, as per the following example:
-  use *databricks* for the *id*
- set the optional *autoCommit* to *true*
- the *schema* name stands actually for the catalog name

# Authorization
Authentication is performed in currently through PATs only.

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