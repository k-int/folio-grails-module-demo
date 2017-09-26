package folio.demo.module

import grails.gorm.MultiTenant;

class FolioResource implements MultiTenant<FolioResource> {
// class FolioResource {

  String id
  String title
  String description

  static constraints = {
  }

  static mapping = {
    table 'fdm_resource'
    id(column:'fdmr_id', generator: 'uuid')
    title column:'fdmr_title'
    description column:'fdmr_description'
  }
}
