package folio.demo.module

class FolioResource {

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
