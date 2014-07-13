package com.websudos.phantom.udt

import scala.collection.mutable.{ ArrayBuffer => MutableArrayBuffer, SynchronizedBuffer => MutableSyncBuffer }
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }
import scala.reflect.runtime.universe.Symbol

import com.websudos.phantom.column.AbstractColumn
import com.datastax.driver.core.Row

abstract class AbstractField[@specialized(Int, Double, Float, Long, Boolean, Short) T] {

}


abstract class UDT[T <: UDT[T]] extends AbstractColumn[T] {

  private[this] lazy val _fields: MutableArrayBuffer[AbstractField[_]] = new MutableArrayBuffer[AbstractField[_]] with MutableSyncBuffer[AbstractField[_]]

  def apply(row: Row): T

  private[this] lazy val _name: String = {
    getClass.getName.split("\\.").toList.last.replaceAll("[^$]*\\$\\$[^$]*\\$[^$]*\\$|\\$\\$[^\\$]*\\$", "").dropRight(1)
  }

  val cassandraType = _name

  private[this] val instanceMirror = cm.reflect(this)
  private[this] val selfType = instanceMirror.symbol.toType

  // Collect all column definitions starting from base class
  private[this] val columnMembers = MutableArrayBuffer.empty[Symbol]

  Lock.synchronized {
    selfType.baseClasses.reverse.foreach {
      baseClass =>
        val baseClassMembers = baseClass.typeSignature.members.sorted
        val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< ru.typeOf[AbstractField[_]])
        baseClassColumns.foreach(symbol => if (!columnMembers.contains(symbol)) columnMembers += symbol)
    }

    columnMembers.foreach {
      symbol =>
        val column = instanceMirror.reflectModule(symbol.asModule).instance
        _fields += column.asInstanceOf[AbstractField[_]]
    }
  }
}

private[phantom] object Lock