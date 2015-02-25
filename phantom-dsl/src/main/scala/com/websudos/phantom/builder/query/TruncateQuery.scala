package com.websudos.phantom.builder.query

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable

class TruncateQuery[Table <: CassandraTable[Table, _], Record](table: Table, val qb: CQLQuery, row: Row => Record) extends ExecutableStatement {

}
