package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[builder] class DeleteQueryBuilder {
  def delete(table: String): CQLQuery = {
    CQLQuery(CQLSyntax.delete)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(table)
  }

  def deleteColumn(table: String, column: String): CQLQuery = {
    CQLQuery(CQLSyntax.delete)
      .forcePad.append(column)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(table)
  }
}
