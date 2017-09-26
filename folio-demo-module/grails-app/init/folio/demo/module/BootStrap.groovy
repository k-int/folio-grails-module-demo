package folio.demo.module

import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import javax.sql.DataSource
import grails.gorm.multitenancy.*

class BootStrap {

  def grailsApplication
  def tenantAdminService
  def dataSource

  def init = { servletContext ->
    log.debug("Reporting config from folio_globals.yaml: ${grailsApplication.config.testsection.message}");


    // The datasource connection can have whatever last set schema was used left in play, so force a switch to the public schema
    dataSource.getConnection().createStatement().execute('set schema \'public\'');

    // log.debug("apply core migrations");
    // applyCoreMigrations()

    log.debug("Ensure test tenants are present");
    try {
      tenantAdminService.createTenant('test1');
      tenantAdminService.createTenant('test2');

      log.debug("Set up some test data");
      Tenants.withId('test1'+'_grails_demo_module') {
        FolioResource.findByTitle('Brain of the Firm')  ?: new FolioResource(title:'Brain of the Firm',description:'A book').save(flush:true, failOnError:true)
      }
 
      Tenants.withId('test2'+'_grails_demo_module') {
        FolioResource.findByTitle('Platform for Change') ?: new FolioResource(title:'Platform for Change',description:'A book').save(flush:true, failOnError:true);
      }
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

    log.debug("BootStrap::init completed");
  }


  def destroy = {
  }

  /**
   * A hangover from when we used to store a DB user level table of tenants. Left here as a guide for when we need
   * to upgrade tenant scheas
   */
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
      gl.defaultSchema = 'public'
      gl.databaseChangeLogTableName = "grails_folio_module_changelog"
      gl.databaseChangeLogLockTableName = "grails_folio_module_changelog_lock"
      gl.afterPropertiesSet() // this runs the update command
    } catch (Exception e) {
        log.error("Exception trying to create new schema tables for module core", e)
        throw e
    }
  }
}
