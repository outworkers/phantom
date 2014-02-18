package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder

import com.newzly.phantom.CassandraTable
import com.newzly.phantom.query.QueryCondition
import com.newzly.phantom.keys.PartitionKey


abstract class Column[Owner <: CassandraTable[Owner, Record], Record, @specialized(Int, Double, Float, Long, Boolean, Short) T](val table: CassandraTable[Owner, Record]) extends AbstractColumn[T] {

  table.addColumn(this)

  def optional(r: Row): Option[T]

  def apply(r: Row): T = optional(r).getOrElse(throw new Exception(s"can't extract required value for column '$name'"))

  def eqs (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.eq(this.name, this.toCType(value)))
  }

  def lt (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.lt(this.name, this.toCType(value)))
  }

  def gt (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.gt(this.name, this.toCType(value)))
  }
}
