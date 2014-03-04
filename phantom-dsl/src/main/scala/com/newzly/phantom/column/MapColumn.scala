package com.newzly.phantom.column

import java.util.{ Map => JMap }
import scala.annotation.implicitNotFound
import scala.collection.breakOut
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.newzly.phantom.query.QueryAssignment
import com.datastax.driver.core.querybuilder.QueryBuilder

@implicitNotFound(msg = "Type ${K} and ${V} must be a Cassandra primitives")
class MapColumn[Owner <: CassandraTable[Owner, Record], Record, K: CassandraPrimitive, V: CassandraPrimitive](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Map[K, V]](table) {

  val cassandraType = s"map<${CassandraPrimitive[K].cassandraType}, ${CassandraPrimitive[V].cassandraType}>"
  def toCType(values: Map[K, V]): JMap[AnyRef, AnyRef] = values.map {
    case (k, v) => CassandraPrimitive[K].toCType(k) -> CassandraPrimitive[V].toCType(v)
  }.asJava

  override def apply(r: Row): Map[K, V] = {
    optional(r).getOrElse(Map.empty[K, V])
  }

  def optional(r: Row): Option[Map[K, V]] = {
    val ki = implicitly[CassandraPrimitive[K]]
    val vi = implicitly[CassandraPrimitive[V]]
    Option(r.getMap(name, ki.cls, vi.cls)).map(_.asScala.map {
      case (k, v) =>
        ki.fromCType(k.asInstanceOf[AnyRef]) -> vi.fromCType(v.asInstanceOf[AnyRef])
    }(breakOut) toMap)
  }
}
