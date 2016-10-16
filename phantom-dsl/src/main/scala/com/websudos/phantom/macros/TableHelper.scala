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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom.macros

import com.websudos.phantom.SelectTable
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.dsl.CassandraTable

import scala.reflect.macros.blackbox

trait TableHelper[T <: CassandraTable[T, R], R] {

  def tableName: String

  //def fromRow(row: Row): R

  def fields(table: T): Set[AbstractColumn[_]]
}

object TableHelper {
  implicit def fieldsMacro[T <: CassandraTable[T, R], R]: TableHelper[T, R] = macro TableHelperMacro.macroImpl[T, R]
}

/**()
    val tableSym = typeOf[CassandraTable[_, _]].typeSymbol
    val selectTable = typeOf[SelectTable[_, _]].typeSymbol
    val rootConn = typeOf[SelectTable[_, _]].typeSymbol

    val exclusions: Symbol => Option[Symbol] = s => {
      val sig = s.typeSignature.typeSymbol

      if (sig == tableSym || sig == selectTable || sig == rootConn) {
        None
      } else {
        Some(s)
      }
    }

    val nameSources = exclude[T, CassandraTable[_, _]](exclusions).filterNot(sym => {
      val named = sym.name.decodedName.toString
      knownList.contains(named)
    })
    println(s"Found ${nameSources.size} naming sources for ${name}")
    nameSources.foreach(s => println(s.name.decodedName.toString))
  */


@macrocompat.bundle
class TableHelperMacro(override val c: blackbox.Context) extends MacroUtils(c) {

  import c.universe._

  val knownList = List("Any", "Object", "RootConnector")

  val tableSym = typeOf[CassandraTable[_, _]].typeSymbol
  val selectTable = typeOf[SelectTable[_, _]].typeSymbol
  val rootConn = typeOf[SelectTable[_, _]].typeSymbol

  val exclusions: Symbol => Option[Symbol] = s => {
    val sig = s.typeSignature.typeSymbol

    if (sig == tableSym || sig == selectTable || sig == rootConn) {
      None
    } else {
      Some(s)
    }
  }

  def findRealTable(tpe: Type, target: Symbol): Option[Symbol] = {
    tpe.baseClasses.find(clz => {
      val base = getDirectBase(clz)

      if (base.isEmpty) {
        c.abort(
          c.enclosingPosition,
          s"Could not find a direct ancestor for type ${tpe.typeSymbol.name.decodedName.toString}"
        )
      }

      base.head == target
    })
  }


  def macroImpl[T : WeakTypeTag, R : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    val sourceName = tpe.typeSymbol.name.decodedName
    val rTpe = weakTypeOf[R]
    val name = tpe.typeSymbol.asType.name.toTermName

    val colTpe = tq"com.websudos.phantom.column.AbstractColumn[_]"

    /*
    val tableName = nameSources match {
      case sym :: Nil => sym.name.decodedName.toString
      case _ => c.abort(
        c.enclosingPosition,
        s"Unable to infer table name, found ${nameSources.size} possible naming sources"
      )
    }*/

    val realTable = findRealTable(tpe, tableSym)

    val finalName = realTable match {
      case Some(tb) => {
        val initial = tb.asType.name.decodedName.toString
        // Lower case the first letter of the type
        // This will make sure the macro derived name is compatible
        // with the old school namin structure used in phantom.
        initial(0).toLower + initial.drop(1)
      }
      case None => c.abort(c.enclosingPosition, s"Could not find out the name of ${sourceName.toString}")
    }

    val accessors = filterMembers[T, AbstractColumn[_]](exclusions)
      .map(_.asTerm.name).map(tm => q"table.${tm.toTermName}")

    val rowType = tq"com.datastax.driver.core.Row"
    val strTpe = tq"java.lang.String"

    q"""
       new com.websudos.phantom.macros.TableHelper[$tpe, $rTpe] {
          def tableName: $strTpe = ${finalName}

          def fields(table: $tpe): scala.collection.immutable.Set[$colTpe] = {
            scala.collection.immutable.Set.apply[$colTpe](..$accessors)
          }
       }
     """
  }

}