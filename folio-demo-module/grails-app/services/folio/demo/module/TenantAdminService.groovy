package folio.demo.module

import groovy.sql.Sql
import grails.rest.*
import grails.converters.*
import javax.sql.DataSource
import liquibase.Liquibase
import liquibase.database.Database
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import java.sql.Connection
import java.sql.ResultSet
import org.grails.datastore.gorm.jdbc.schema.SchemaHandler

// import grails.gorm.multitenancy.*
// @WithoutTenant


class TenantAdminService {

  final static String SCHEMA_SUFFIX = '_grails_demo_module'

  def hibernateDatastore
  def dataSource

  public void createTenant(String tenantId) {

      String new_schema_name = tenantId+SCHEMA_SUFFIX;
      try {
        hibernateDatastore.getDatastoreForConnection(new_schema_name)
        log.debug("Module already registered for tenant");
      }
      catch ( org.grails.datastore.mapping.core.exceptions.ConfigurationException ce ) {
        log.debug("register module for tenant");
        createAccountSchema(new_schema_name);
        updateAccountSchema(new_schema_name);

        hibernateDatastore.addTenantForSchema(new_schema_name)
      }
  }

  void createAccountSchema(String tenantId) {
    Sql sql = null
    try {
        sql = new Sql(dataSource as DataSource)
        sql.withTransaction {
            sql.execute("create schema ${tenantId}" as String)
        }
    } catch (Exception e) {
        log.error("Unable to create schema for tenant $tenantId", e)
        throw e
    } finally {
        sql?.close()
    }
  }

  void freshenAllTenantSchemas() {
    ResultSet schemas = dataSource.getConnectio().getMetaData().getSchemas()
    while(schemas.next()) {
      String schema_name = schemas.getString("TABLE_SCHEM")
      if ( schema_name.endsWith(SCHEMA_SUFFIX) ) {
        // It's one to update
        // Now try create the tables for the schema
        try {
          GrailsLiquibase gl = new GrailsLiquibase(applicationContext)
          gl.dataSource = applicationContext.getBean("dataSource", DataSource)
          gl.dropFirst = false
          gl.changeLog = 'module-tenant-changelog.groovy'
          gl.contexts = []
          gl.labels = []
          gl.defaultSchema = schema_name
          gl.databaseChangeLogTableName = 'grails_demo_folio_module_tenant_changelog'
          gl.databaseChangeLogLockTableName = 'grails_demo_folio_module_tenant_changelog_lock'
          gl.afterPropertiesSet() // this runs the update command
        } catch (Exception e) {
            log.error("Exception trying to create new account schema tables for $tenantId", e)
            throw e
        }
      }
    }

  }

  void updateAccountSchema(String tenantId) {
    def applicationContext = grails.util.Holders.applicationContext

    // Now try create the tables for the schema
    try {
      GrailsLiquibase gl = new GrailsLiquibase(applicationContext)
      gl.dataSource = applicationContext.getBean("dataSource", DataSource)
      gl.dropFirst = false
      gl.changeLog = 'module-tenant-changelog.groovy'
      gl.contexts = []
      gl.labels = []
      gl.defaultSchema = tenantId
      gl.databaseChangeLogTableName = 'grails_demo_folio_module_tenant_changelog'
      gl.databaseChangeLogLockTableName = 'grails_demo_folio_module_tenant_changelog_lock'
      gl.afterPropertiesSet() // this runs the update command
    } catch (Exception e) {
        log.error("Exception trying to create new account schema tables for $tenantId", e)
        throw e
    }

    try {
      hibernateDatastore.addTenantForSchema(tenantId)
    } catch (Exception e) {
      log.error("Exception adding tenant schema for ${tenantId}", e)
      throw e
    }
  }

}
