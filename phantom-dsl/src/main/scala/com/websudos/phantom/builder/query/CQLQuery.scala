package com.websudos.phantom.builder.query

import com.websudos.phantom.builder.CQLSyntax

case class CQLQuery(queryString: String) {
  def append(st: String): CQLQuery = CQLQuery(queryString + st)
  def append(st: CQLQuery): CQLQuery = append(st.queryString)
  def append[T](list: T)(implicit ev1: T => TraversableOnce[String]): CQLQuery = CQLQuery(queryString + list.mkString(", "))

  def appendEscape(st: String): CQLQuery = append(escape(st))
  def appendEscape(st: CQLQuery): CQLQuery = appendEscape(st.queryString)

  def appendSingleQuote(st: String): CQLQuery = append(singleQuote(st))
  def appendSingleQuote(st: CQLQuery): CQLQuery = append(singleQuote(st.queryString))

  def appendIfAbsent(st: String): CQLQuery = if (queryString.endsWith(st)) CQLQuery(queryString) else append(st)
  def appendIfAbsent(st: CQLQuery): CQLQuery = appendIfAbsent(st.queryString)

  def prepend(st: String): CQLQuery = CQLQuery(st + queryString)
  def prepend(st: CQLQuery): CQLQuery = prepend(st.queryString)

  def escape(st: String): String = "`" + st + "`"
  def singleQuote(st: String): String = "'" + st + "'"

  def spaced: Boolean = queryString.endsWith(" ")
  def pad: CQLQuery = if (spaced) this else CQLQuery(queryString + " ")
  def forcePad: CQLQuery = CQLQuery(queryString + " ")
  def trim: CQLQuery = CQLQuery(queryString.trim)

  def wrap(str: String): CQLQuery = pad.append(CQLSyntax.`(`).append(str).append(CQLSyntax.`)`)
  def wrap(query: CQLQuery): CQLQuery = wrap(query.queryString)
  def wrap[T](list: T)(implicit ev1: T => TraversableOnce[String]): CQLQuery = wrap(list.mkString(", "))
  def wrapEscape(list: List[String]): CQLQuery = wrap(list.map(escape).mkString(", "))
}

object CQLQuery {
  def empty: CQLQuery = CQLQuery("")

  def escape(str: String): String = "'" + str + "'"

  def apply(collection: TraversableOnce[String]): CQLQuery = CQLQuery(collection.mkString(", "))
}
