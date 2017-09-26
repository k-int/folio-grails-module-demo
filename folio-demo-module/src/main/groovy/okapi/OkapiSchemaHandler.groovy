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

    final static String SCHEMA_SUFFIX = '_grails_demo_module'
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

          // Iterate through all schemas, ignore any that don't end SCHEMA_SUFFIX, add those that do to the result.
          // This may be the place to run migrations, or it may be better to do that in bootstrap.
          ResultSet schemas = connection.getMetaData().getSchemas()
          while(schemas.next()) {
            String schema_name = schemas.getString("TABLE_SCHEM")
            if ( schema_name.endsWith(SCHEMA_SUFFIX) ) {
              schemaNames.add(schema_name)
            }
          }
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

