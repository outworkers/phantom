/*
 *
 *  * Copyright 2014 websudos ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
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

sealed class QueryBuilder {

}


object CQLQueryBuilder extends QueryBuilder
