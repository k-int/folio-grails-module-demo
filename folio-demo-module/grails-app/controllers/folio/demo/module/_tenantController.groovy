package folio.demo.module


import grails.rest.*
import grails.converters.*

class _tenantController {
	static responseFormats = ['json', 'xml']
	
  def index() { 
    println("_tenantController::index ${params}");
    def result = [:]
    render result as JSON
  }
}
