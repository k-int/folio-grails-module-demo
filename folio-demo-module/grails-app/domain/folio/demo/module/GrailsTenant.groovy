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
    gt_module column:'gt_module'
    gt_tenant column:'gt_tenant'
  }
}
