package folio.demo.module

import groovy.sql.Sql
import grails.rest.*
import grails.converters.*
import javax.sql.DataSource
import liquibase.Liquibase
import liquibase.database.Database
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
// import grails.gorm.multitenancy.*
// @WithoutTenant


class TenantAdminService {

  def hibernateDatastore
  def dataSource

  public void createTenant(String tenantId) {

    GrailsTenant.withNewSession() {

      String new_schema_name = tenantId+'_grails_demo_module';
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
