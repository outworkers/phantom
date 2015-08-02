package com.websudos.phantom.db

import com.datastax.driver.core.Session
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.ExecutableStatementList
import com.websudos.phantom.connectors.{KeySpace, KeySpaceDef}

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}

private object Lock

abstract class DatabaseImpl(protected[this] val connector: KeySpaceDef) {

  private[this] lazy val _tables: MutableArrayBuffer[CassandraTable[_, _]] = new MutableArrayBuffer[CassandraTable[_, _]]

  implicit val space: KeySpace = new KeySpace(connector.name)

  implicit lazy val session: Session = connector.session

  def tables: Set[CassandraTable[_, _]] = _tables.toSet

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
        } else if (symbol.isTerm){
          instanceMirror.reflectField(symbol.asTerm).get
        }
        _tables += table.asInstanceOf[CassandraTable[_, _]]
    }
  }

  def autocreate(): ExecutableStatementList = {
    new ExecutableStatementList(_tables.toSeq.map {
      table => table.create.ifNotExists().qb
    })
  }

  def autotruncate(): ExecutableStatementList = {
    new ExecutableStatementList(_tables.toSeq.map {
      table => table.truncate().qb
    })
  }
}


