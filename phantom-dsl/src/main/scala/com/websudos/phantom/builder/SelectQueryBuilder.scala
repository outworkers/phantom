package com.websudos.phantom.builder

import com.websudos.phantom.builder.query.CQLQuery

sealed class OrderingModifier {

  def ascending(column: String): CQLQuery = {

    CQLQuery(column).forcePad.append(CQLSyntax.Ordering.asc)
  }

  def descending(column: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Ordering.desc)
  }

  def orderBy(qb: CQLQuery, clause: CQLQuery) = {
    Utils.concat(qb, clause)
  }
}

private[builder] class SelectQueryBuilder {

  case object Ordering extends OrderingModifier

  def select(tableName: String): CQLQuery = {
    CQLQuery(CQLSyntax.select)
      .pad.append("*").forcePad
      .append(CQLSyntax.from)
      .forcePad.appendEscape(tableName)
  }

  def select(tableName: String, names: String*): CQLQuery = {
    CQLQuery(CQLSyntax.select)
      .pad.append(names)
      .forcePad.append(CQLSyntax.from)
      .forcePad.appendEscape(tableName)
  }

  def count(tableName: String, names: String*): CQLQuery = {
    CQLQuery(CQLSyntax.select)
      .forcePad.append(CQLSyntax.count)
      .pad.wrap(names)
      .forcePad.append(CQLSyntax.from)
      .forcePad.appendEscape(tableName)
  }

  def distinct(tableName: String, names: String*): CQLQuery = {
    CQLQuery(CQLSyntax.select)
      .forcePad.append(CQLSyntax.distinct)
      .pad.append(names)
      .forcePad.append(CQLSyntax.from)
      .forcePad.appendEscape(tableName)
  }

  def select(tableName: String, clause: CQLQuery) = {
    CQLQuery(CQLSyntax.select)
      .pad.append(clause)
      .pad.append(CQLSyntax.from)
      .pad.appendEscape(tableName)
  }

  def allowFiltering(qb: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.allowFiltering)
  }


}
