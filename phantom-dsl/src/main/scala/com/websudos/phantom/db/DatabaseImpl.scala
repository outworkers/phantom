/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.db

import com.datastax.driver.core.Session
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.ExecutableStatementList
import com.websudos.phantom.connectors.{KeySpace, KeySpaceDef}

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.concurrent.blocking

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

  def shutdown(): Unit = {
    blocking {
      com.websudos.phantom.Manager.shutdown()
      session.getCluster.close()
      session.close()
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


