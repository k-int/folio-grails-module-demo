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
    def applicationContext = Holders.applicationContext

    // Now try create the tables for the schema
    try {
      GrailsLiquibase gl = new GrailsLiquibase(applicationContext)
      gl.dataSource = applicationContext.getBean("dataSource", DataSource)
      gl.dropFirst = false
      gl.changeLog = 'changelog-m.groovy'
      gl.contexts = []
      gl.labels = []
      gl.defaultSchema = tenantId
      gl.databaseChangeLogTableName = defaultChangelogTableName
      gl.databaseChangeLogLockTableName = defaultChangelogLockTableName
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
