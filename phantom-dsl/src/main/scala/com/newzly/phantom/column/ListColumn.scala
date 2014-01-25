package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.datastax.driver.core.Row

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class ListColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](override val table: CassandraTable[Owner, Record]) extends Column[Owner, Record, List[RR]](table) {
  val cassandraType = s"list<${CassandraPrimitive[RR].cassandraType}>"

  def toCType(values: List[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).asJava

  override def apply(r: Row): List[RR] = {
    optional(r).getOrElse(List.empty[RR])
  }

  def optional(r: Row): Option[List[RR]] = {
    val primitive = implicitly[CassandraPrimitive[RR]]
    Option(r.getList(name, primitive.cls).asScala.toList.map(el => primitive.fromCType(el.asInstanceOf[AnyRef])))
  }

}
