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
