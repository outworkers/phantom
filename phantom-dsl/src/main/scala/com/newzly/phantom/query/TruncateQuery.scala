package com.newzly.phantom.query

import com.datastax.driver.core.querybuilder.Truncate
import com.newzly.phantom.CassandraTable

class TruncateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Truncate) extends ExecutableStatement {
}