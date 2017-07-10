package folio.demo.module


import grails.rest.*
import grails.converters.*

class OkapiController {
	static responseFormats = ['json', 'xml']
	
  def index() { 
  }

  def tenant() {
    println("OkapiController::tenant ${params}");
  }
}
