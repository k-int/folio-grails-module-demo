

# Directory structure

We're assuming you're working inside a "folio" workspace, and that you will create a new module in that folder at the same level as the checked out okapi:

    some/path
        folio
            folio-demo-module
            folio-sample-modules
            mod-users
            okapi
            raml-module-builder

# Prerequisites

This demo is created using grails 3.2.9. We've found sdkman/gvm the best way to manage grails versions. You can obtian gvm from http://sdkman.io/. After downloading and installing 

    gvm install grails 3.2.9

will get you the grails env you need.

Folio. This demo was created using okapi core with commit e1b1df335e5fff65fb036224907ba505f3f99cf0 from https://github.com/folio-org/okapi.git

# Steps to reproduce

We create the skeletal app with

    grails --profile rest-api create-app folio-demo-module

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

 

