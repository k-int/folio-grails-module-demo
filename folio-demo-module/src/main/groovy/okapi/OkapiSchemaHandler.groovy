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
        // useSchemaStatement = "SET SCHEMA %s"
        useSchemaStatement = "SET search_path TO %s,public"
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
          connection
                .createStatement()
                .execute(useStatement)
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
        Collection<String> schemaNames = []
        Connection connection = null
        try {
            // This method is not great - it will try to add all schemas, and that isn't what we want for okapi 
            connection = dataSource.getConnection()
        //     ResultSet schemas = connection.getMetaData().getSchemas()
        //     while(schemas.next()) {
        //         schemaNames.add(schemas.getString("TABLE_SCHEM"))
        //     }
            schemaNames.add('test1');
            schemaNames.add('test2');
        } finally {
            try {
                connection?.close()
            } catch (Throwable e) {
                log.debug("Error closing SQL connection: $e.message", e)
            }
        }
        return schemaNames
    }
}

