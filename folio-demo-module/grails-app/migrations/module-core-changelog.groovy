databaseChangeLog = {

    changeSet(author: "ibbo (generated)", id: "1505910270410-1") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "ibbo (generated)", id: "1505910270410-3") {
        createTable(tableName: "grails_module_tenant") {
            column(name: "gt_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "gt_tenant", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "gt_module", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ibbo (generated)", id: "1505910270410-5") {
        addPrimaryKey(columnNames: "gt_id", constraintName: "grails_module_tenant_pkey", tableName: "grails_module_tenant")
    }
}
