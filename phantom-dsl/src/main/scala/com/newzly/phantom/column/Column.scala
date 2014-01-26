package com.newzly.phantom.column

import java.util.concurrent.atomic.AtomicBoolean
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder

import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.newzly.phantom.query.QueryCondition


abstract class Column[Owner <: CassandraTable[Owner, Record], Record, T](table: CassandraTable[Owner, Record]) extends AbstractColumn[T] {


  protected[this] lazy val _isKey = new AtomicBoolean(false)
  protected[this] lazy val _isPrimaryKey = new AtomicBoolean(false)

  type ValueType = T

  table.addColumn(this)

  def getTable: CassandraTable[Owner, Record] = table

  override def apply(r: Row): T =
    optional(r).getOrElse(throw new Exception(s"can't extract required value for column '$name'"))

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
