package folio.demo.module

class FolioResource {

  String tenantId
  String title
  String description

  static constraints = {
  }

  static mapping = {
    table 'fdm_resource'
    id column:'fdmr_id'
    tenantId column:'fdmr_tenant_id'
    title column:'fdmr_title'
    description column:'fdmr_description'
  }
}
