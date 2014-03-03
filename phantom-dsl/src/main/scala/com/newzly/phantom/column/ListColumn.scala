package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.datastax.driver.core.Row
import com.newzly.phantom.query.{QueryAssignment, QueryCondition}
import com.datastax.driver.core.querybuilder.QueryBuilder

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class ListColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, List[RR]](table) {
  val cassandraType = s"list<${CassandraPrimitive[RR].cassandraType}>"
  val primitive = implicitly[CassandraPrimitive[RR]]

  def toCType(values: List[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).asJava

  override def apply(r: Row): List[RR] = {
    optional(r).getOrElse(List.empty[RR])
  }

  def optional(r: Row): Option[List[RR]] = {
    Option(r.getList(name, primitive.cls).asScala.toList.map(el => primitive.fromCType(el.asInstanceOf[AnyRef])))
  }

  def append(value: RR): QueryAssignment = {
    QueryAssignment(QueryBuilder.append(this.name, primitive.toCType(value)))
  }

  def prepend(value: RR): QueryAssignment = {
    QueryAssignment(QueryBuilder.prepend(this.name, primitive.toCType(value)))
  }


}
