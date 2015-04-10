package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax






private[builder] class IndexModifiers extends BaseModifiers {

  def eqs(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.eqs, value)
  }

  def ==(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.eqs, value)
  }

  def lt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lt, value)
  }

  def lte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lte, value)
  }

  def gt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gt, value)
  }

  def gte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gte, value)
  }

  def in(column: String, values: String*): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def in(column: String, values: List[String]): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def fcall(name: String, params: String*): CQLQuery = {
    CQLQuery(name).append(Utils.join(params))
  }

  def token(name: String): String = {
    CQLQuery(CQLSyntax.token).wrap(name).queryString
  }

  def where(qb: CQLQuery, condition: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.where, condition)
  }

  def and(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.and, clause)
  }

}
