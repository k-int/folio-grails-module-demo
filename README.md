

# Directory structure

We're assuming you're working inside a "folio" workspace, and that you will create a new module in that folder at the same level as the checked out okapi. We're going to end
up with a structure like the one below. Don't worry if you don't see all these files/directories at the start - this is where we will end up.

    some/path                                          -- Maybe your home dir or dev area
        folio                                          -- A folder for folio work
            folio_globals.yaml                         -- A config file where we can share global config for spring-boot based apps
            folio-grails-module-demo                   -- the grails based folio module we are building (And is checked into this git repo)
            folio-sample-modules                       -- The defacto folio demo module
            mod-users                                  -- the users module
            okapi                                      -- okapi core
            raml-module-builder                        -- the raml module builder, in case you want to play

# Prerequisites

This demo is created using grails 3.2.9. We've found sdkman/gvm the best way to manage grails versions. You can obtian gvm from http://sdkman.io/. After downloading and installing 

    gvm install grails 3.2.9

will get you the grails env you need.

Folio. This demo was created using okapi core with commit e1b1df335e5fff65fb036224907ba505f3f99cf0 from https://github.com/folio-org/okapi.git

# Steps to reproduce

## ZFR

We create the skeletal app with

    grails --profile rest-api create-app folio-demo-module

## Demo controller (Folio service)

Next we set up a hello controller

    cd folio-demo-module
    grails create-controller hello

This should create 2 new files (grails uses some conventions which convert a hypenated app name into packages. You can override this behavior by explicitly specifying
the package in create-controller): grails-app/controllers/folio/demo/module/HelloController.groovy and src/test/groovy/folio/demo/module/HelloControllerSpec.groovy

We will update the hello controller so that it's index method returns a JSON document - edit grails-app/controllers/folio/demo/module/HelloController.groovy as follows:

    package folio.demo.module


    import grails.rest.*
    import grails.converters.*

    class HelloController {
        static responseFormats = ['json', 'xml']
        def index() { 
           def result=[message:'hello world'];
           return result;
        }
    }

## Integrate with global config

We need to prove that we can interact with some shared global configuraiton. Update grails-app/init/folio/demo/module/BootStrap.groovy to print a message at app startup time
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

And set up the configuration file we are going to use - ../../folio_globals.yaml

    testsection:
        message: Test Global Configuration Worked

It would be nice to prove to ourselves that the global configuration is really overriding the local config, and not simply being used instead of it. Update
grails-app/conf/application.yml and add the following three lines to the top of the file (Which will then contiue with the grails: section as below).

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


## Build the prod war

    grails prod war

Will make a ./build/libs/folio-demo-module-0.1.war and build/libs/folio-demo-module-0.1.war.original for you. The .war is an executable jar file that is intended to be deployed
as a microservice. The .war can actually be made into an init-script using embeddedLaunchScript, tho thats not idiomatically compatible with FOLIO.

### Test the war In different environments

    java -jar ./build/libs/folio-demo-module-0.1.war --spring.config.location=file:../../folio_globals.yaml

You should see output like

    ibbo@angstrom:~/dev/folio/folio-grails-module-demo/folio-demo-module$ java -jar ./build/libs/folio-demo-module-0.1.war --spring.config.location=file:../../folio_globals.yaml
    Reporting config from folio_globals.yaml: Test Global Configuration Worked
    Grails application running at http://localhost:8080 in environment: production

Use ctrl-c to exit the app, and relaunch using

    java -jar ./build/libs/folio-demo-module-0.1.war

You should see output like

    ibbo@angstrom:~/dev/folio/folio-grails-module-demo/folio-demo-module$ java -jar ./build/libs/folio-demo-module-0.1.war
    Reporting config from folio_globals.yaml: Test Local Configuration Worked
    Grails application running at http://localhost:8080 in environment: production

OK - we've got a way to share global config that overrides app defaults. spring-boot idioms are different to FOLIO ones, so this mechanism provides us a way to
behave differently in different environments - following the spring-boot idioms when running locally, but a FOLIO global config when running in the FOLIO produciton environment.

## More Configuration

### FOLIO Descriptors

Sample deployment/module descriptors are provided which assume a foler layout as set out at the head of this document, and expose a single hello service.


#### Deployment Descriptor

    {
      "srvcId": "grails-helloworld-module",
      "nodeId": "localhost"
    }


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
        }
      ],
      "launchDescriptor": {
        "exec": "java -jar -Dserver.port=%p ../folio-grails-module-demo/folio-demo-module/build/libs/folio-grails-module-0.1.war"
      }
    }

## Datasource configuration

Datasource configration in FOLIO is currently not as idiomatic as grails, although local conventions are starting to appear around the use of embedded
postgres and an environment variable. At the time of writing, mod_users (With this behaviour most likely inherited from raml_module_builer) accepts a db_connection
environment variable

    db_connection=/home/ibbo/dev/folio/postgres-conf.json

This is raml_module_builder special sauce, and whilst it may become a defacto standard for FOLIO, it's not cleanly compatible with spring boot (Which grails 3 uses under the hood).
Grails 3 apps support the SPRING_APPLICATION_JSON environment variable and the spring.application.json system property as an additional way to pass a JSON configuration
object to the app. The underlying spring boot docs can be found here: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html.

It seems that there is a genine and sound desire to be able to centralise some FOLIO configuration - so that dev, test and production environments can have clean shared config.
Grails already provides a clean mechanism for per-environment configuration. Therefore, it seems appropriate to suggest an idiomatic approach to the configuration challenge
for spring-boot based FOLIO modules

### Proposal: Shared config for spring boot based FOLIO applications

As stated in the spring boot docs,--spring.config.location can be used to specify files and/or directories which will be added to the list of paths searched.

The example from the spring boot docs is:

java -jar myproject.jar --spring.config.location=classpath:/default.properties,classpath:/override.properties

We propose the follwing as a spring-boot idiomatic approach to config

 --spring.config.location=file:///some/path/folio_globals.yaml


