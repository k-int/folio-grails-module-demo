package folio.demo.module

import grails.gorm.MultiTenant;

class GrailsTenant {

  String id
  String module
  String tenant
  String schemaName

  static constraints = {
  }

  static mapping = {
    table 'grails_module_tenant'
    id(column:'gt_id', generator: 'uuid')
    module column:'gt_module'
    tenant column:'gt_tenant'
    schemaName column:'gt_schema_name'
  }
}
