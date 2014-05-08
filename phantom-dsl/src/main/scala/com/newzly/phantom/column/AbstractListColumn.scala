package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import java.util.{ List => JavaList }
import scala.collection.JavaConverters._
import scala.util.Try

abstract class AbstractListColumn[Owner <: CassandraTable[Owner, Record], Record, RR](table: CassandraTable[Owner, Record])
    extends Column[Owner, Record, List[RR]](table) with CollectionValueDefinition[RR] {

  def valuesToCType(values: Iterable[RR]): JavaList[AnyRef] =
    values.map(valueToCType).toList.asJava

  override def toCType(values: List[RR]): AnyRef = valuesToCType(values)

  override def apply(r: Row): List[RR] = {
    optional(r).getOrElse(Nil)
  }

  override def optional(r: Row): Option[List[RR]] = {
    Try {
      r.getList(name, valueCls).asScala.map(e => valueFromCType(e.asInstanceOf[AnyRef])).toList
    }.toOption
  }
}
