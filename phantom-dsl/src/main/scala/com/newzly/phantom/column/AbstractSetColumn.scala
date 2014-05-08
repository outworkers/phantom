package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import java.util.{ Set => JavaSet }
import scala.collection.JavaConverters._
import scala.util.Try

abstract class AbstractSetColumn[Owner <: CassandraTable[Owner, Record], Record, RR](table: CassandraTable[Owner, Record])
    extends Column[Owner, Record, Set[RR]](table) with CollectionValueDefinition[RR] {

  def valuesToCType(values: Iterable[RR]): JavaSet[AnyRef] =
    values.map(valueToCType).toSet.asJava

  override def toCType(values: Set[RR]): AnyRef = valuesToCType(values)

  override def apply(r: Row): Set[RR] = {
    optional(r).getOrElse(Set.empty[RR])
  }

  override def optional(r: Row): Option[Set[RR]] = {
    Try {
      r.getSet(name, valueCls).asScala.map(e => valueFromCType(e.asInstanceOf[AnyRef])).toSet
    }.toOption
  }
}
