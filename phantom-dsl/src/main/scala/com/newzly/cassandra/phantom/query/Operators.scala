package com.newzly.cassandra.phantom.query

import com.newzly.cassandra.phantom.AbstractColumn
import com.datastax.driver.core.querybuilder.{QueryBuilder, Clause}

object Operators{
  def EQ[T,RR](c: T => AbstractColumn[RR], value: RR)(table:T): Clause = {
    val col = c(table)
    QueryBuilder.eq(col.name, col.toCType(value))
  }

  def GT[T,RR](c: T => AbstractColumn[RR], value: RR)(table:T): Clause = {
    val col = c(table)
    QueryBuilder.gt(col.name, col.toCType(value))
  }

  def LT[T,RR](c: T => AbstractColumn[RR], value: RR)(table:T): Clause = {
    val col = c(table)
    QueryBuilder.lt(col.name, col.toCType(value))
  }



}
