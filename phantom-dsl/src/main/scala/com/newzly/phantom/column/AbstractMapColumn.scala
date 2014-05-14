package com.newzly.phantom.column

import java.util.{ Map => JavaMap }
import scala.collection.breakOut
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable

abstract class AbstractMapColumn[Owner <: CassandraTable[Owner, Record], Record, K, V](table: CassandraTable[Owner, Record])
    extends Column[Owner, Record, Map[K, V]](table) with CollectionValueDefinition[V] {

  def keyCls: Class[_]

  def keyToCType(v: K): AnyRef

  def keyFromCType(c: AnyRef): K

  def valuesToCType(values: Traversable[(K, V)]): JavaMap[AnyRef, AnyRef] =
    values.map({ case (k, v) => keyToCType(k) -> valueToCType(v) }).toMap.asJava

  override def toCType(values: Map[K, V]): AnyRef = valuesToCType(values)

  override def apply(r: Row): Map[K, V] = {
    optional(r).getOrElse(Map.empty[K, V])
  }

  def optional(r: Row): Option[Map[K, V]] = {
    Option(r.getMap(name, keyCls, valueCls)).map(_.asScala.map {
      case (k, v) =>
        keyFromCType(k.asInstanceOf[AnyRef]) -> valueFromCType(v.asInstanceOf[AnyRef])
    }(breakOut) toMap)
  }
}
