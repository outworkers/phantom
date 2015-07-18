package com.websudos.phantom.db

import com.datastax.driver.core.{ResultSet, Session}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.connectors.{KeySpace, KeySpaceDef}

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer}
import scala.concurrent.Future
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}

private object Lock

abstract class DatabaseImpl(connector: KeySpaceDef) {

  private[this] lazy val _tables: MutableArrayBuffer[CassandraTable[_, _]] = new MutableArrayBuffer[CassandraTable[_, _]]

  implicit val space: KeySpace = new KeySpace(connector.name)

  implicit lazy val session: Session = connector.session

  Lock.synchronized {

    val instanceMirror = cm.reflect(this)
    val selfType = instanceMirror.symbol.toType

    // Collect all column definitions starting from base class
    val columnMembers = MutableArrayBuffer.empty[Symbol]
    selfType.baseClasses.reverse.foreach {
      baseClass =>
        val baseClassMembers = baseClass.typeSignature.members.sorted
        val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< ru.typeOf[CassandraTable[_, _]])
        baseClassColumns.foreach(symbol => if (!columnMembers.contains(symbol)) columnMembers += symbol)
    }

    columnMembers.foreach {
      symbol =>
        val table =  if (symbol.isModule) {
          instanceMirror.reflectModule(symbol.asModule).instance
        } else {
          instanceMirror.reflectModule(symbol.asModule).symbol
        }
        _tables += table.asInstanceOf[CassandraTable[_, _]]
    }
  }

  def autocreate(): Future[List[ResultSet]] = {
    Future.sequence(_tables.toList.map {
      table => table.create.ifNotExists().future()
    })
  }

  def autotruncate(): Future[List[ResultSet]] = {
    Future.sequence(_tables.toList.map {
      table => table.truncate.future()
    })
  }
}


