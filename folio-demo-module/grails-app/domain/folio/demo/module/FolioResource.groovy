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
