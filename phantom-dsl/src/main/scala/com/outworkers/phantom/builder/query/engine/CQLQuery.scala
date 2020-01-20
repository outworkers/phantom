/*
 * Copyright 2013 - 2020 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.query.engine

import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.builder.syntax.CQLSyntax.Symbols
import com.outworkers.phantom.connectors.KeySpaceCQLQuery
import scala.collection.compat._

case class CQLQuery(override val queryString: String) extends KeySpaceCQLQuery {

  val defaultSep = ", "

  def instance(st: String): CQLQuery = CQLQuery(st)

  def nonEmpty: Boolean = queryString.nonEmpty

  def append(st: String): CQLQuery = instance(queryString + st)
  def append(st: CQLQuery): CQLQuery = append(st.queryString)

  def append[M[X] <: IterableOnce[X]](list: M[String], sep: String = defaultSep): CQLQuery = {
    instance(queryString + list.iterator.mkString(sep))
  }

  def appendEscape(st: String): CQLQuery = append(escape(st))
  def appendEscape(st: CQLQuery): CQLQuery = appendEscape(st.queryString)

  def terminate: CQLQuery = appendIfAbsent(CQLSyntax.Symbols.semicolon)

  def appendSingleQuote(st: String): CQLQuery = append(singleQuote(st))
  def appendSingleQuote(st: CQLQuery): CQLQuery = append(singleQuote(st.queryString))

  def appendIfAbsent(st: String): CQLQuery = if (queryString.endsWith(st)) instance(queryString) else append(st)
  def appendIfAbsent(st: CQLQuery): CQLQuery = appendIfAbsent(st.queryString)

  def prepend(st: String): CQLQuery = instance(st + queryString)
  def prepend(st: CQLQuery): CQLQuery = prepend(st.queryString)

  def prependIfAbsent(st: String): CQLQuery = if (queryString.startsWith(st)) instance(queryString) else prepend(st)
  def prependIfAbsent(st: CQLQuery): CQLQuery = prependIfAbsent(st.queryString)

  def escape(st: String): String = "`" + st + "`"
  def singleQuote(st: String): String = "'" + st.replaceAll("'", "''") + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: CQLQuery = if (spaced) this.asInstanceOf[CQLQuery] else instance(queryString + " ")
  def bpad: CQLQuery = prependIfAbsent(" ")

  def forcePad: CQLQuery = instance(queryString + " ")
  def trim: CQLQuery = instance(queryString.trim)

  def wrapn(str: String): CQLQuery = append(Symbols.`(`).append(str).append(Symbols.`)`)
  def wrapn(query: CQLQuery): CQLQuery = wrapn(query.queryString)
  def wrap(str: String): CQLQuery = pad.wrapn(str)
  def wrap(query: CQLQuery): CQLQuery = wrap(query.queryString)

  def wrapn[M[X] <: IterableOnce[X]](
    col: M[String],
    sep: String = defaultSep
  ): CQLQuery = wrapn(col.iterator mkString sep)

  def wrap[M[X] <: IterableOnce[X]](
    col: M[String],
    sep: String = defaultSep
  ): CQLQuery = wrap(col.iterator mkString sep)

  override def toString: String = queryString
}

object CQLQuery {

  def empty: CQLQuery = CQLQuery("")

  def escape(str: String): String = "'" + str.replaceAll("'", "''") + "'"

  def apply(collection: IterableOnce[String]): CQLQuery = CQLQuery(collection.iterator.mkString(", "))
}
