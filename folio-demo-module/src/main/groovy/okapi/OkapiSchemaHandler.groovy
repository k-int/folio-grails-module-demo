package okapi

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.sql.DataSource
import java.sql.Connection
import java.sql.ResultSet
import org.grails.datastore.gorm.jdbc.schema.SchemaHandler

/**
 * Resolves the schema names
 *
 * @author Graeme Rocher
 * @since 6.0
 *
 * @See https://github.com/grails/gorm-hibernate5/blob/master/grails-datastore-gorm-hibernate5/src/main/groovy/org/grails/orm/hibernate/HibernateDatastore.java
 */
@CompileStatic
@Slf4j
class OkapiSchemaHandler implements SchemaHandler {

    final String useSchemaStatement
    final String createSchemaStatement
    final String defaultSchemaName

    OkapiSchemaHandler() {
        useSchemaStatement = "SET SCHEMA '%s'"
        // useSchemaStatement = "SET search_path TO %s,public"
        createSchemaStatement = "CREATE SCHEMA %s"
        defaultSchemaName = "public"
    }

    OkapiSchemaHandler(String useSchemaStatement, String createSchemaStatement, String defaultSchemaName) {
        this.useSchemaStatement = useSchemaStatement
        this.createSchemaStatement = createSchemaStatement
        this.defaultSchemaName = defaultSchemaName
    }

    @Override
    void useSchema(Connection connection, String name) {
        log.debug("useSchema");
        String useStatement = String.format(useSchemaStatement, name)
        log.debug("Executing SQL Set Schema Statement: ${useStatement}")
        
        try {
          log.debug("Try");

          // Gather all the schemas
          ResultSet schemas = connection.getMetaData().getSchemas()
          Collection<String> schemaNames = []
          while(schemas.next()) {
            schemaNames.add(schemas.getString("TABLE_SCHEM"))
          }

          if ( schemaNames.contains(name) ) {
            // The assumption seems to be that this will throw an exception if the schema does not exist, but pg silently continues...
            connection
                  .createStatement()
                  .execute(useStatement)
          }
          else {
            throw new RuntimeException("Attempt to use schema ${name} that does not exist according to JDBC metadata");
          }
        }
        catch ( Exception e ) {
          log.error("problem trying to use schema - \"${useStatement}\"",e)
          // Rethrow
          throw e
        }

        log.debug("useSchema completed OK");
    }

    @Override
    void useDefaultSchema(Connection connection) {
        log.debug("useDefaultSchema");
        useSchema(connection, defaultSchemaName)
    }

    @Override
    void createSchema(Connection connection, String name) {
        String schemaCreateStatement = String.format(createSchemaStatement, name)
        log.debug("Executing SQL Create Schema Statement: ${schemaCreateStatement}")
        connection
                .createStatement()
                .execute(schemaCreateStatement)
    }

    @Override
    Collection<String> resolveSchemaNames(DataSource dataSource) {
        // If this is called by HibernateDatastore.java then the next step will be for the
        // addTenantForSchemaInternal method to be called for this db
        log.debug("OkapiSchemaHandler::resolveSchemaNames called")
        Collection<String> schemaNames = []
        Connection connection = dataSource.getConnection()
        try {

          ResultSet schemas = connection.getMetaData().getSchemas()
          while(schemas.next()) {
            String schema_name = schemas.getString("TABLE_SCHEM")
            if ( schema_name.endsWith('_grails_demo_module') ) {
              schemaNames.add(schema_name)
            }
          }

          // java.sql.DatabaseMetaData db_metadata = connection.getMetaData()

          // if ( db_metadata.getTables(null, null, 'grails_module_tenant', null).next() != false ) {
          // This method is not great - it will try to add all schemas, and that isn't what we want for okapi 
          //   log.debug("Switch schema");
          //   def stmnt = connection.createStatement().execute('set schema \'public\'');
          //   log.debug("List tenants");
          //   ResultSet schemas = connection.createStatement().executeQuery('select gt_schema_name from grails_module_tenant where gt_module=\'demo\'');
          //   while(schemas.next()) {
          //     schemaNames.add(schemas.getString(1))
          //   }
          //   log.debug("All done");
          // }
          // else {
          //   log.warn("No grails_module_tenant detected. Assuming this is the first time this module has run, so no tenants exist yet. App setup will create the necessary artefacts");
          // }
        } finally {
            try {
                connection.createStatement().execute('set schema \'public\'')
                connection?.close()
            } catch (Throwable e) {
                log.debug("Error closing SQL connection: $e.message", e)
            }
        }
        log.debug("OkapiSchemaHandler::resolveSchemaNames called - returning ${schemaNames}")
        return schemaNames
    }
}

