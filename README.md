
# Purpose of this repository

This repository is a demo of how to get a spring-boot app (grails 3 in this case) working as a [FOLIO](https://github.com/folio-org) module. It's the first in a series that set out how [K-int](http://www.k-int.com) intend to build modules for the FOLIO ecosystem (as external contributors). It should be considered separate to our work as a part of the core team. This project makes some different choices, and has different constraints to, the core FOLIO effort. This is the first in a series that will explain how we expect domain modelling and other spring-boot idioms like security to operate in the FOLIO environment. Our hope is to make it extremely easy to rapidly develop FOLIO modules using the high productivity frameworks that web developers expect to leverage.

The latest revision of this repo will assume you are using the FOLIO vagrant build as your baseline host environment. This README is in a state of flux whilst that transition happens.

## Important Note

The default branch for this project has been updated to be grails_3_3_0_manual_schema_per_tenant this branch now demonstrates an approach to implementing schema-per-tenant in folio/okapi modules based on the grails ecosystem. Important points to note are:

* The management of the database schema is done by liquibase database migrations. This allows the app to have one set of "Core" migrations, and separate migrations for tenants.
* The demo module no longer uses a shared "grails_module_tenant" but instead uses database metadata to enumerate the tenant/module instances
* The approch taken with migrations allows very fine grained control over schemas and when they are upgraded, this is much prefered to the traditional grails approach of always comparing the schema at app startup time. With high volume tenants, this becomes very prohibitive. The approach adopted here (Deeply inspired by this SO post: https://stackoverflow.com/questions/44176219/unable-to-dynamically-provision-a-gorm-capable-data-source-in-grails-3 and massive kudos to SO user jondow)
* the okapi tenant controller calls the tenant admin service to provision the schema and confiure tables.

* I need to check the rest of this readme to bring it up to date for 3_3_0 and the schema per tenant example.

There is a walkthrough of this readme [here](https://youtu.be/wf84CLgnUEA)

# Directory structure - Quickstart

The old directory structure is gone, replaced with the following as taken from https://github.com/folio-org/folio-ansible/blob/master/doc/index.md

    mkdir /home/user/folio-vagrant
    cd /home/user/folio-vagrant
    vagrant init folio/testing
    vagrant up

Check out this project somewhere else (EG /home/user/dev) , cd into folio-demo-module and run

    grails prod war

OR

    gradle war -Penv=prod

then cd ../scripts and run

    ./deploy_to_vagrant.sh /home/user/folio-vagrant

This will copy all the necessary files and manually deploy the Module descriptor
    

# Building this project from first principles

This demo is created using grails 3.3.0. We've found sdkman/gvm the best way to manage grails versions. You can obtian gvm from http://sdkman.io/. After downloading and installing 

    gvm install grails 3.3.0

will get you the grails env you need. Different versions of grails are supported by different branches of this git project. The default branch will reflect current k-int
target environments.

Folio. This demo was created using okapi core with commit e1b1df335e5fff65fb036224907ba505f3f99cf0 from https://github.com/folio-org/okapi.git

# Steps to reproduce

## ZFR (Zero Functionality Release)

From inside some/path/folio/folio-grails-module-demo We create the skeletal app with

    grails --profile rest-api create-app folio-demo-module
    cd folio-demo-module

## Demo controller (Folio service)

Next we set up a hello controller

    grails create-controller hello

This should create 2 new files (grails uses some conventions which convert a hypenated app name into packages. You can override this behavior by explicitly specifying
the package in create-controller): grails-app/controllers/folio/demo/module/HelloController.groovy and src/test/groovy/folio/demo/module/HelloControllerSpec.groovy

Better yet, to set the default package for all created resources using the grails cli, update grails-app/conf/application.yml and set the default package:
```yml
grails:
    codegen:
        defaultPackage: my.custom.package
...
```

We will update the hello controller so that it's index method returns a JSON document - edit [grails-app/controllers/folio/demo/module/HelloController.groovy](https://github.com/k-int/folio-grails-module-demo/blob/master/folio-demo-module/grails-app/controllers/folio/demo/module/HelloController.groovy) as follows:

    package folio.demo.module


    import grails.rest.*
    import grails.converters.*

    class HelloController {
        static responseFormats = ['json', 'xml']
        def index() { 
           def result=[message:'hello world'];
           render result as JSON;
        }
    }

## OKAPI core support - tenant module

Create a controller which will support the okapi core interfaces (underscore tenant mapping)

    grails create-controller okapi

Implement a tenant action - [OkapiController.groovy](https://github.com/k-int/folio-grails-module-demo/blob/master/folio-demo-module/grails-app/controllers/folio/demo/module/OkapiController.groovy)

    package folio.demo.module


    import grails.rest.*
    import grails.converters.*

    class OkapiController {
      static responseFormats = ['json', 'xml']

      def index() { 
      }
    
      // GET And DELETE verbs with header X-Okapi-Tenant indicate activation of this module for a given tenant.
      def tenant() {
        println("OkapiController::tenant ${params} ${request.getHeader('X-Okapi-Tenant')}");
        def result = [:]
        render result as JSON
      }

    }

And modify [grails-app/controllers/folio/demo/module/UrlMappings.groovy](https://github.com/k-int/folio-grails-module-demo/blob/master/folio-demo-module/grails-app/controllers/folio/demo/module/UrlMappings.groovy) to map the _ path to our okapi controller

        "/_/tenant"(controller: 'okapi', action:'tenant')


## Demo Domain Class

The major point of spring-boot and hibernate based apps is the ability to construct database centric processing applications. lets create a domain class to demo these capabilities
and prove that our datasource config is working as expected.

    grails create-domain-class folioResource

This will generate [grails-app/domain/folio/demo/module/FolioResource.groovy and src/test/groovy/folio/demo/module/FolioResourceSpec.groovy](https://github.com/k-int/folio-grails-module-demo/blob/master/folio-demo-module/grails-app/domain/folio/demo/module/FolioResource.groovy). We will fill out FolioResource as follows

    package folio.demo.module

    import grails.gorm.MultiTenant;

    class FolioResource implements MultiTenant<FolioResource> {

      String title
      String description
    
      static constraints = {
      }
    
      static mapping = {
        table 'fdm_resource'
        id column:'fdmr_id'
        title column:'fdmr_title'
        description column:'fdmr_description'
      }
    }

We're overriding the default generated table and column names. This is a convention we (people developing spring-boot modules for folio) might want to consider adopting as a community to make sure that modules
don't clash when operating in the default schema.

## Integrate with global config

We need to prove that we can interact with some shared global configuraiton. Update [grails-app/init/folio/demo/module/BootStrap.groovy](https://github.com/k-int/folio-grails-module-demo/blob/master/folio-demo-module/grails-app/init/folio/demo/module/BootStrap.groovy) to print a message at app startup time
which reports a value from our global config.

    package folio.demo.module

    class BootStrap {

      def grailsApplication

      def init = { servletContext ->
        println("Reporting config from folio_globals.yaml: ${grailsApplication.config.testsection.message}");
      }

      def destroy = {
      }
    }

And set up the configuration file we are going to use - ../../folio_globals.yaml. If you followed the instructions in step one this is done already.

    testsection:
        message: Test Global Configuration Worked

It would be nice to prove to ourselves that the global configuration is really overriding the local config, and not simply being used instead of it. Update
[grails-app/conf/application.yml](https://github.com/k-int/folio-grails-module-demo/blob/master/folio-demo-module/grails-app/conf/application.yml) and add the following three lines to the top of the file (Which will then contiue with the grails: section as below).

    ---
    testsection:
      message: Test Local Configuration Worked
    ---
    grails:
        profile: rest-api
        codegen:
            defaultPackage: folio.demo.module
        spring:

When our app starts up, the bootstrap init closure will report the configuration value grailsApplication.config.testsection.message we expect that depending upon the
config usedm we will see "Test Local Configuration Worked" or "Test Global Configuration Worked". See Test The War below.

# Multi Tenant Specials

See
* https://github.com/k-int/folio-grails-module-demo/blob/grails_3_3_0_manual_schema_per_tenant/folio-demo-module/src/main/groovy/okapi/OkapiSchemaHandler.groovy
* https://github.com/k-int/folio-grails-module-demo/blob/grails_3_3_0_manual_schema_per_tenant/folio-demo-module/src/main/groovy/okapi/OkapiTenantResolver.groovy 
* also note the special config in https://github.com/k-int/folio-grails-module-demo/blob/grails_3_3_0_manual_schema_per_tenant/folio-demo-module/grails-app/conf/application.yml (tenantResolverClass and schemaHandler)


## Build the prod war

    grails prod war

Will make a ./build/libs/folio-demo-module-0.1.war and build/libs/folio-demo-module-0.1.war.original for you. The .war is an executable jar file that is intended to be deployed
as a microservice. The .war can actually be made into an init-script using embeddedLaunchScript, tho thats not idiomatically compatible with FOLIO. The .original is a traditional
.war file intended to be deployed in a container. For our purposes here, we want the .war variant.

### Test the war In different environments

    java -jar ./build/libs/folio-demo-module-0.1.war --spring.config.location=file:../../folio_globals.yaml

You should see output like

    ibbo@angstrom:~/dev/folio/folio-grails-module-demo/folio-demo-module$ java -jar ./build/libs/folio-demo-module-0.1.war --spring.config.location=file:../../folio_globals.yaml
    Reporting config from folio_globals.yaml: *Test Global Configuration Worked*
    Grails application running at http://localhost:8080 in environment: production

Use ctrl-c to exit the app, and relaunch using

    java -jar ./build/libs/folio-demo-module-0.1.war

You should see output like

    ibbo@angstrom:~/dev/folio/folio-grails-module-demo/folio-demo-module$ java -jar ./build/libs/folio-demo-module-0.1.war
    Reporting config from folio_globals.yaml: *Test Local Configuration Worked*
    Grails application running at http://localhost:8080 in environment: production

OK - we've got a way to share global config that overrides app defaults. spring-boot idioms are different to FOLIO ones, so this mechanism provides us a way to
behave differently in different environments - following the spring-boot idioms when running locally, but a FOLIO global config when running in the FOLIO produciton environment.

## More Configuration

### FOLIO Descriptors

Sample deployment/module descriptors are provided which assume a foler layout as set out at the head of this document, and expose a single hello service along side the tenant support endpoint.



#### Module Descriptor

    {
      "id": "grails-helloworld-module",
      "name": "grails-hello-world",
      "provides": [
        {
          "id": "grails-hello-world",
          "version": "1.0",
          "handlers" : [
            {
              "methods": [ "GET" ],
              "pathPattern": "/hello"
            }
          ]
        },
        {
          "id": "_tenant",
          "version": "1.0",
          "interfaceType" : "system",
          "handlers": [
            {
              "methods": ["POST"],
              "pathPattern": "/_/tenant"
            }, {
              "methods": ["DELETE"],
              "pathPattern": "/_/tenant"
            }
          ]
        }
      ]
    }

#### Deployment Descriptor

Things to note: --server.port= is the spring boot way to specify a port number. --spring-config-location allows us to specify a config file which will override anything
set in conf/application.yml. We use this to set the FOLIO-wide postgres connection we wish to use. The file will look like this::

    {
      "srvcId": "grails-helloworld-module",
      "nodeId": "localhost",
      "descriptor": {
        "exec": "java -jar ../folio-grails-module-demo/folio-demo-module/build/libs/folio-demo-module-0.1.war --server.port=%p --spring.config.location=file:../folio_globals.yaml"
      }
    }

N.B. Also that spring boot properties can be overridden on the command-line with --property.name=value so --dataSource.username=alternate should allow you to override the datasource username

#### Deployment Descriptor - with IDE.

The okapi guide at https://github.com/folio-org/okapi/blob/master/doc/guide.md says

    It is also possible to refer to an already-launched process (maybe running in your development IDE), by POSTing a DeploymentDescriptor to /_/discovery, with no nodeId and no LaunchDescriptor, but with the URL where the module is running.

So, deploying the module descriptor, using "grails run-app" and then posting the following deployment descriptor should get our module working with debugging support.

    {
      "srvcId": "grails-helloworld-module",
      "descriptor": {
        "uri": "http://localhost:8080/"
      }
    }


But in practice, we generate the DeploymentDescriptor and the ModuleDescriptor from the build.gradle file by processing the template files in src/okapi/*.template

## About databases

some parts of FOLIO core relies upon assumptions that don't fit well with some hibernate idioms. In particualr concrete assumptions about the physical implementation
of multi-tenant deployment are implicit in the design (Database dialects, partitioning strategies), and push towards postgres specific schema-per-tenant setup. This doesn't sit cleanly with hibernates
database agnostic approach to per-environment configuration / db platform agnosticism. For this app, we're taking a concious decision to divert slightly from that approach.

In order to run this sample app, the following postgres config is expected (These need to be run as the postgres DBA user). The SUPERUSER role parts are included here
because they are needed if you wish to run mod_user from the same configuration (You might want to). Currently, this demo does not require superuser privis, but this
area of FOLIO feels less well defined currently.

    CREATE USER folio WITH PASSWORD 'folio' SUPERUSER CREATEDB INHERIT LOGIN;

    CREATE DATABASE folio;
    GRANT ALL PRIVILEGES ON DATABASE folio to folio;

    CREATE DATABASE foliodev;
    GRANT ALL PRIVILEGES ON DATABASE foliodev to folio;

### This is the folio vagrant image equivalent setup

    CREATE USER folio_admin WITH PASSWORD 'folio_admin' SUPERUSER CREATEDB INHERIT LOGIN;
    CREATE DATABASE okapi_modules;
    GRANT ALL PRIVILEGES ON DATABASE okapi_modules to folio_admin;



GORM Multi-Tennancy documentation can be found here: http://gorm.grails.org/latest/hibernate/manual/index.html#_schema_per_tenant


### About multi-tenant

This example project now uses schema-per-tenant and database migrations

## Datasource configuration

In order to connect to folio, we will need to add this dependency to build.gradle (You can add it under the h2 dependency, which can be left in)

    runtime 'postgresql:postgresql:9.1-901-1.jdbc4'


Datasource configration in FOLIO is currently not as idiomatic as grails, although local conventions are starting to appear around the use of embedded
postgres and an environment variable. At the time of writing, mod_users (With this behaviour most likely inherited from raml_module_builer) accepts a db_connection
environment variable

    db_connection=/home/ibbo/dev/folio/postgres-conf.json

This is raml_module_builder special sauce, and whilst it may become a defacto standard for FOLIO, it's not cleanly compatible with spring boot (Which grails 3 uses under the hood).
Grails 3 apps support the SPRING_APPLICATION_JSON environment variable and the spring.application.json system property as an additional way to pass a JSON configuration
object to the app. The underlying spring boot docs can be found here: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html.

It seems that there is a genuine and sound desire to be able to centralise some FOLIO configuration - so that dev, test and production environments can have clean shared config.
Grails already provides a clean mechanism for per-environment configuration. Therefore, it seems appropriate to suggest an idiomatic approach to the configuration challenge
for spring-boot based FOLIO modules

Example of ../../folio_globals.yaml config with (Per env) DB settings. Because we deploy the prod war, thats all that is included.

    testsection:
      message: Test Global Configuration Worked
    environments:
        production:
            dataSource:
                dbCreate: update
                username: folio
                password: folio
                driverClassName: org.postgresql.Driver
                dialect: org.hibernate.dialect.PostgreSQLDialect
                url: jdbc:postgresql://localhost:5432/folio
                schemaHandler: okapi.OkapiSchemaHandler

These values should match whatever you have used in your ../../postgres-conf.json. Re running


    java -jar ./build/libs/folio-demo-module-0.1.war --spring.config.location=file:../../folio_globals.yaml

With this config should cause an fgm_resource table to appear in the default schema of your postgres folio user. Check this out with

    psql -h your.pg.host -U folio

Useful postgres commands
  \dn - List schemas
  \dt - List schemas
  set schema 'schemaname'; - use schemaname as the default schema, don't look in public
  set search_path schema-1,public;  - Look in schema-1 first, then in public

Where your.pg.host may be localhost or wherever you run postgres. The folio password in the default setup is folio. \dt and \d fdm_resource should respond below

    folio=# \dt
               List of relations
     Schema |     Name     | Type  | Owner 
    --------+--------------+-------+-------
     public | fdm_resource | table | folio
    (1 row)
    
    folio=# \d fdm_resource;
                  Table "public.fdm_resource"
          Column      |          Type          | Modifiers 
    ------------------+------------------------+-----------
     fdmr_id          | bigint                 | not null
     version          | bigint                 | not null
     fdmr_description | character varying(255) | not null
     fdmr_tenant_id   | character varying(255) | not null
     fdmr_title       | character varying(255) | not null
    Indexes:
        "fdm_resource_pkey" PRIMARY KEY, btree (fdmr_id)
    
    folio=# \q
    
# FOLIO Proper

from your folio directory, cd into okapi and start up a new okapi. You might find this best done in a separate shell window for ease of debug

    cd okapi
    # mvn install if you need to build
    java -Dloglevel=DEBUG -jar okapi-core/target/okapi-core-fat.jar dev

This will launch a transient okapi (No persistent storage of tenants and modules). Lets verify that by starting a new shell, and running

    curl -i -w '\n' -X GET http://localhost:9130/_/proxy/modules

and 

    curl -i -w '\n' -X GET http://localhost:9130/_/proxy/tenants


The following commands (Ripped off from http://dev.folio.org/curriculum/02_initialize_okapi_from_the_command_line). For brevity, this project provides a
json config file for the test tenant in testlib-tenant.json ::

    curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @testlib-tenant.json http://localhost:9130/_/proxy/tenants

Lets install the new grails app module descriptor

    curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @ModuleDescriptor.json http://localhost:9130/_/proxy/modules

And install the deployment descriptor for our new module

    curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @DeploymentDescriptor.json http://localhost:9130/_/discovery/modules

Once this has completed, the app should have started using the global datasource config in folio_globals.yaml. In the okapi console, you should see a message like

    Reporting config from folio_globals.yaml: Test Global Configuration Worked
    Grails application running at http://localhost:9131 in environment: production

Activate the module for the tenant

    curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @testlib-mod-activation.json http://localhost:9130/_/proxy/tenants/testlib/modules

A shortcut [setup.sh](https://github.com/k-int/folio-grails-module-demo/blob/master/setup.sh) script is provided to do these steps with less typing.

Finally - call the spring-boot service via okapi pretending tobe testlib tenant:

    curl -i -w '\n' -X GET -H 'X-Okapi-Tenant: testlib' http://localhost:9130/hello




* dev notes

grails dbm-generate-gorm-changelog changelog.groovy

Can be used on a clean database to generate a full set of migration scripts .
