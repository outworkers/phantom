package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder

import com.newzly.phantom.CassandraTable
import com.newzly.phantom.query.QueryCondition
import com.newzly.phantom.keys.PartitionKey


abstract class Column[Owner <: CassandraTable[Owner, Record], Record, T](val table: CassandraTable[Owner, Record]) extends AbstractColumn[T] {

  table.addColumn(this)

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

  implicit class PartitionTokenHelper(p: PartitionKey){

    def ltToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.lt(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token",p.asInstanceOf[Column[_,_,T]].toCType(value))))
    }

    def gtToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.gt(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token",p.asInstanceOf[Column[_,_,T]].toCType(value))))
    }

    def eqsToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.eq(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token",p.asInstanceOf[Column[_,_,T]].toCType(value))))
    }
  }
}
