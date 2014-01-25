package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class SeqColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](override val table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Seq[RR]](table) {

  val cassandraType = s"list<${CassandraPrimitive[RR].cassandraType}>"
  def toCType(values: Seq[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).toSeq.asJava

  override def apply(r: Row): Seq[RR] = {
    optional(r).getOrElse(Seq.empty)
  }

  def optional(r: Row): Option[Seq[RR]] = {
    val i = implicitly[CassandraPrimitive[RR]]
    Option(r.getList(name, i.cls)).map(_.asScala.map(e => i.fromCType(e.asInstanceOf[AnyRef])).toIndexedSeq)
  }
}
