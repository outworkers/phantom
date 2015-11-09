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

  /**
   * Creates a CQL 2.2 JSON insert clause using a pre-serialized JSON string.
   * @param init The initialization query of the Insert clause, generally comprising the "INSERT INTO tableName" part.
   * @param jsonString The pre-serialized JSON string to insert into the Cassandra table.
   * @return A CQL query with the JSON prefix appended to the insert.
   */
  def json(init: CQLQuery, jsonString: String): CQLQuery = {
    init.pad.append("JSON").pad.append(CQLQuery.escape(jsonString))
  }

  def columns(list: List[CQLQuery]) = {
    CQLQuery.empty.wrapn(list.map(_.queryString))
  }

  def values(list: List[CQLQuery]) = {
    CQLQuery(CQLSyntax.values).wrapn(list.map(_.queryString))
  }

}
