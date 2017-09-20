package folio.demo.module

import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import javax.sql.DataSource

class BootStrap {

  def grailsApplication
  def tenantAdminService

  def init = { servletContext ->
    log.debug("Reporting config from folio_globals.yaml: ${grailsApplication.config.testsection.message}");
    log.debug("apply core migrations");

    applyCoreMigrations()

    tenantAdminService.createTenant('test1');
    tenantAdminService.createTenant('test2');
  }

  def destroy = {
  }

  def applyCoreMigrations() {
    // Now try create the tables for the schema
    try {
      def applicationContext = grails.util.Holders.applicationContext

      GrailsLiquibase gl = new GrailsLiquibase(applicationContext)
      gl.dataSource = applicationContext.getBean("dataSource", DataSource)
      gl.dropFirst = false
      gl.changeLog = 'module-core-changelog.groovy'
      gl.contexts = []
      gl.labels = []
      // gl.defaultSchema = tenantId
      gl.databaseChangeLogTableName = "grails_folio_module_changelog"
      gl.databaseChangeLogLockTableName = "grails_folio_module_changelog_lock"
      gl.afterPropertiesSet() // this runs the update command
    } catch (Exception e) {
        log.error("Exception trying to create new account schema tables for module core", e)
        throw e
    }
  }
}
