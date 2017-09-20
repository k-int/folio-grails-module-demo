package folio.demo.module

import grails.gorm.MultiTenant;

class GrailsTenant {

  String module
  String tenant

  static constraints = {
  }

  static mapping = {
    table 'grails_module_tenant'
    id column:'gt_id'
    module column:'gt_module'
    tenant column:'gt_tenant'
  }
}
