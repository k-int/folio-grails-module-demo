package folio.demo.module

import groovy.sql.Sql
import grails.rest.*
import grails.converters.*
import javax.sql.DataSource
import liquibase.Liquibase
import liquibase.database.Database
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase


class TenantAdminService {

  def hibernateDatastore
  def dataSource

  public void createTenant(String tenantId) {

    def gt = GrailsTenant.findByModuleAndTenant('demo',tenantId);
    if ( gt == null ) {
      log.debug("Module already registered for tenant");
      createAccountSchema(tenantId);
      updateAccountSchema(tenantId);
      new GrailsTenant(module:'demo', tenant:tenantId).save(flush:true, failOnError:true);
    }
    else {
      log.debug("Module already registered for tenant");
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
