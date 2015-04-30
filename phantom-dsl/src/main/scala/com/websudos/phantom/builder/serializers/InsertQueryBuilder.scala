package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[phantom] trait InsertQueryBuilder {
  def insert(table: String): CQLQuery = {
    CQLQuery(CQLSyntax.insert)
      .forcePad.append(CQLSyntax.into)
      .forcePad.append(table)
  }

  def insert(table: CQLQuery): CQLQuery = {
    insert(table.queryString)
  }

  def columns(list: List[CQLQuery]) = {
    CQLQuery.empty.wrapn(list.map(_.queryString))
  }

  def values(list: List[CQLQuery]) = {
    CQLQuery(CQLSyntax.values).wrapn(list.map(_.queryString))
  }

}
