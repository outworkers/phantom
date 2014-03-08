package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder

import com.newzly.phantom.CassandraTable
import com.newzly.phantom.query.QueryCondition
import com.newzly.phantom.keys.PartitionKey


abstract class Column[Owner <: CassandraTable[Owner, Record], Record, T](val table: CassandraTable[Owner, Record]) extends AbstractColumn[T] {

  table.addColumn(this)

  def optional(r: Row): Option[T]

  def apply(r: Row): T = optional(r).getOrElse(throw new Exception(s"can't extract required value for column '$name'"))
}
