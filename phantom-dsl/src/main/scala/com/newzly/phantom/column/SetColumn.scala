package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }
import com.newzly.phantom.query.QueryAssignment
import com.datastax.driver.core.querybuilder.QueryBuilder

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class SetColumn[Owner <: CassandraTable[Owner, Record], Record, RR : CassandraPrimitive](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Set[RR]](table) {

  val cassandraType = s"set<${CassandraPrimitive[RR].cassandraType}>"
  def toCType(values: Set[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).asJava

  override def apply(r: Row): Set[RR] = {
    optional(r).getOrElse(Set.empty)
  }

  def optional(r: Row): Option[Set[RR]] = {
    val i = implicitly[CassandraPrimitive[RR]]
    Option(r.getSet(name, i.cls)).map(_.asScala.map(e => i.fromCType(e.asInstanceOf[AnyRef])).toSet)
  }
}
