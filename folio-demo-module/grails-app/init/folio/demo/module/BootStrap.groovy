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

}
