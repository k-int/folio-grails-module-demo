package folio.demo.module

class BootStrap {

  def grailsApplication

  def init = { servletContext ->
    println("Reporting config from folio_globals.yaml: ${grailsApplication.config.testsection.message}");
  }

  def destroy = {
  }
}
