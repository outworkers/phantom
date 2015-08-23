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
        } else if (symbol.isTerm) {
          instanceMirror.reflectField(symbol.asTerm).get
        }
        _tables += table.asInstanceOf[CassandraTable[_, _]]
    }
  }

  /**
   * Returns a list of executable statements that will be parallelized with futures
   * to create the entire database schema in a single call.
   *
   * Every future in the statement list will contain the CQL schema generation query
   * for a single table. Processing order is not guaranteed however the tables
   * are generally processed in the order they are written in, even though
   * ordering is not a guarantee required at this level of an application.
   *
   * @return An executable statement list that can be used with Scala or Twitter futures to simultaneously
   *         execute an entire sequence of queries.
   */
  def autocreate(): ExecutableStatementList = {
    new ExecutableStatementList(_tables.toSeq.map {
      table => table.create.ifNotExists().qb
    })
  }

  /**
   * Returns a list of executable statements that will be parallelized with futures
   * to drop the entire database schema in a single call.
   *
   * Every future in the statement list will contain the ALTER DROP drop query
   * for a single table. Processing order is not guaranteed however the tables
   * are generally processed in the order they are written in, even though
   * ordering is not a guarantee required at this level of an application.
   *
   * @return An executable statement list that can be used with Scala or Twitter futures to simultaneously
   *         execute an entire sequence of queries.
   */
  def autodrop(): ExecutableStatementList = {
    new ExecutableStatementList(_tables.toSeq.map {
      table => table.alter().drop().qb
    })
  }

  /**
   * Returns a list of executable statements that will be parallelized with futures
   * to truncate the entire database schema in a single call.
   *
   * Every future in the statement list will contain the CQL truncation query
   * for a single table. Processing order is not guaranteed however the tables
   * are generally processed in the order they are written in, even though
   * ordering is not a guarantee required at this level of an application.
   *
   * @return An executable statement list that can be used with Scala or Twitter futures to simultaneously
   *         execute an entire sequence of queries.
   */
  def autotruncate(): ExecutableStatementList = {
    new ExecutableStatementList(_tables.toSeq.map {
      table => table.truncate().qb
    })
  }
}


