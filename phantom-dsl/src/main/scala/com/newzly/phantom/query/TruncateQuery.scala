package com.newzly.phantom.query

import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.querybuilder.{BuiltStatement, Truncate}
import com.newzly.phantom.CassandraTable




class TruncateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Truncate) extends SharedQueryMethods[TruncateQuery[T, R], Truncate](qb) with ExecutableStatement {}