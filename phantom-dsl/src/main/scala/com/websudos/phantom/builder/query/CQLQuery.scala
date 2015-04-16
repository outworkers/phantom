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
package com.websudos.phantom.builder.query

import com.websudos.phantom.builder.syntax.CQLSyntax

case class CQLQuery(queryString: String) {

  def nonEmpty: Boolean = queryString.nonEmpty

  def append(st: String): CQLQuery = CQLQuery(queryString + st)
  def append(st: CQLQuery): CQLQuery = append(st.queryString)
  def append[T](list: T, sep: String = ", ")(implicit ev1: T => TraversableOnce[String]): CQLQuery = CQLQuery(queryString + list.mkString(sep))

  def appendEscape(st: String): CQLQuery = append(escape(st))
  def appendEscape(st: CQLQuery): CQLQuery = appendEscape(st.queryString)

  def terminate(): CQLQuery = appendIfAbsent(";")

  def appendSingleQuote(st: String): CQLQuery = append(singleQuote(st))
  def appendSingleQuote(st: CQLQuery): CQLQuery = append(singleQuote(st.queryString))

  def appendIfAbsent(st: String): CQLQuery = if (queryString.endsWith(st)) CQLQuery(queryString) else append(st)
  def appendIfAbsent(st: CQLQuery): CQLQuery = appendIfAbsent(st.queryString)

  def prepend(st: String): CQLQuery = CQLQuery(st + queryString)
  def prepend(st: CQLQuery): CQLQuery = prepend(st.queryString)

  def prependIfAbsent(st: String): CQLQuery = if (queryString.startsWith(st)) CQLQuery(queryString) else prepend(st)
  def prependIfAbsent(st: CQLQuery): CQLQuery = prependIfAbsent(st.queryString)

  def escape(st: String): String = "`" + st + "`"
  def singleQuote(st: String): String = "'" + st + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: CQLQuery = if (spaced) this else CQLQuery(queryString + " ")
  def bpad = prependIfAbsent(" ")

  def forcePad: CQLQuery = CQLQuery(queryString + " ")
  def trim: CQLQuery = CQLQuery(queryString.trim)

  def wrapn(str: String): CQLQuery = append(CQLSyntax.`(`).append(str).append(CQLSyntax.`)`)
  def wrap(str: String): CQLQuery = pad.append(CQLSyntax.`(`).append(str).append(CQLSyntax.`)`)
  def wrap(query: CQLQuery): CQLQuery = wrap(query.queryString)

  def wrapn[T](list: T)(implicit ev1: T => TraversableOnce[String]): CQLQuery = wrapn(list.mkString(", "))
  def wrap[T](list: T)(implicit ev1: T => TraversableOnce[String]): CQLQuery = wrap(list.mkString(", "))
  def wrapEscape(list: List[String]): CQLQuery = wrap(list.map(escape).mkString(", "))

}

object CQLQuery {
  def empty: CQLQuery = CQLQuery("")

  def escape(str: String): String = "'" + str + "'"

  def apply(collection: TraversableOnce[String]): CQLQuery = CQLQuery(collection.mkString(", "))
}
