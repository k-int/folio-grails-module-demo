package folio.demo.module

import groovy.sql.Sql

import grails.rest.*
import grails.converters.*
import javax.sql.DataSource
import liquibase.Liquibase
import liquibase.database.Database
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase

class OkapiController {

  static responseFormats = ['json', 'xml']
  def hibernateDatastore
  def dataSource

  def index() { 
  }

  // GET And DELETE verbs with header X-Okapi-Tenant indicate activation of this module for a given tenant.
  def tenant() {
    println("OkapiController::tenant ${params} ${request.getHeader('X-Okapi-Tenant')}");
    def result = [:]

    // Lets sort dynamic creation of this tenant
    // HibernateDatastore datastore = ...
    // datastore.addTenantForSchema("myNewSchema")

    render result as JSON
  }

}
