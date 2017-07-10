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
