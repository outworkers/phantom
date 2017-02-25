/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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

import com.outworkers.phantom.builder.syntax.CQLSyntax.Symbols

abstract class AbstractQuery[QT <: AbstractQuery[QT]](val queryString: String) {

  def instance(st: String): QT

  def nonEmpty: Boolean = queryString.nonEmpty

  def append(st: String): QT = instance(queryString + st)
  def append(st: QT): QT = append(st.queryString)

  def append[M[X] <: TraversableOnce[X]](list: M[String], sep: String = ", "): QT = {
    instance(queryString + list.mkString(sep))
  }

  def appendEscape(st: String): QT = append(escape(st))
  def appendEscape(st: QT): QT = appendEscape(st.queryString)

  def terminate: QT = appendIfAbsent(";")

  def appendSingleQuote(st: String): QT = append(singleQuote(st))
  def appendSingleQuote(st: QT): QT = append(singleQuote(st.queryString))

  def appendIfAbsent(st: String): QT = if (queryString.endsWith(st)) instance(queryString) else append(st)
  def appendIfAbsent(st: QT): QT = appendIfAbsent(st.queryString)

  def prepend(st: String): QT = instance(st + queryString)
  def prepend(st: QT): QT = prepend(st.queryString)

  def prependIfAbsent(st: String): QT = if (queryString.startsWith(st)) instance(queryString) else prepend(st)
  def prependIfAbsent(st: QT): QT = prependIfAbsent(st.queryString)

  def escape(st: String): String = "`" + st + "`"
  def singleQuote(st: String): String = "'" + st.replaceAll("'", "''") + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: QT = if (spaced) this.asInstanceOf[QT] else instance(queryString + " ")
  def bpad: QT = prependIfAbsent(" ")

  def forcePad: QT = instance(queryString + " ")
  def trim: QT = instance(queryString.trim)

  def wrapn(str: String): QT = append(Symbols.`(`).append(str).append(Symbols.`)`)
  def wrapn(query: QT): QT = wrapn(query.queryString)
  def wrap(str: String): QT = pad.append(Symbols.`(`).append(str).append(Symbols.`)`)
  def wrap(query: QT): QT = wrap(query.queryString)

  def wrapn[M[X] <: TraversableOnce[X]](list: M[_]): QT = wrapn(list.mkString(", "))
  def wrap[M[X] <: TraversableOnce[X]](list: M[String]): QT = wrap(list.mkString(", "))
  def wrapEscape(list: List[String]): QT = wrap(list.map(escape).mkString(", "))

}
