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
package com.websudos.phantom.query


case class CQLStatement(queryString: String) {
  def append(st: String): CQLStatement = CQLStatement(queryString + st)
  def append(st: CQLStatement): CQLStatement = append(st.queryString)

  def appendEscape(st: String): CQLStatement = append(escape(st))
  def appendEscape(st: CQLStatement): CQLStatement = append(escape(st.queryString))

  def prepend(st: String): CQLStatement = CQLStatement(st + queryString)
  def prepend(st: CQLStatement): CQLStatement = prepend(st.queryString)

  def escape(st: String): String = "'" + st + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: CQLStatement = if (spaced) this else CQLStatement(queryString + " ")
  def forcePad: CQLStatement = CQLStatement(queryString + " ")
  def trim: CQLStatement = CQLStatement(queryString.trim)

  def wrap(str: String): CQLStatement = pad.append(CQLSyntax.`(`).append(str).append(CQLSyntax.`)`)
  def wrap(query: CQLStatement): CQLStatement = wrap(query.queryString)
}


sealed class CQLSyntax {

  val into = "INTO"
  val values = "VALUES"
  val select = "SELECT"
  val writetime = "WRITETIME"

  val create = "CREATE"
  val insert = "INSERT"
  val ifNotExists = "IF NOT EXISTS"

  val where = "WHERE"
  val update = "UPDATE"
  val delete = "DELETE"
  val orderBy = "ORDER BY"
  val limit = "LIMIT"
  val and = "AND"
  val or = "OR"
  val set = "SET"
  val from = "FROM"
  val table = "TABLE"
  val eqs = "="
  val `(` = "("
  val comma = ","
  val `)` = ")"
  val asc = "ASC"
  val desc = "DESC"
}

object CQLSyntax extends CQLSyntax

sealed class CQLQueryBuilder {}

object CQLQueryBuilder extends CQLQueryBuilder
