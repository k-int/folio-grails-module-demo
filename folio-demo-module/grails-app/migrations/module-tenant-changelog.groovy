databaseChangeLog = {

    changeSet(author: "ibbo (generated)", id: "1505910270410-1") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "ibbo (generated)", id: "1505910270410-2") {
        createTable(tableName: "fdm_resource") {
            column(name: "fdmr_id", type: "VARCHAR(32)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fdmr_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "fdmr_description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ibbo (generated)", id: "1505910270410-4") {
        addPrimaryKey(columnNames: "fdmr_id", constraintName: "fdm_resource_pkey", tableName: "fdm_resource")
    }

}
