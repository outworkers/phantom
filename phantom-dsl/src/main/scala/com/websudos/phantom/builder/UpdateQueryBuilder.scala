package com.websudos.phantom.builder

import com.websudos.phantom.builder.query.CQLQuery

private[builder] class UpdateQueryBuilder {

  def `if`(column: String, value: String): CQLQuery = {
    Utils.concat(column, CQLSyntax.Operators.eqs, value)
  }

  def onlyIf(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb, clause)
  }

  private[this] def counterSetter(column: String, op: String, value: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`)
      .forcePad.append(column)
      .forcePad.append(op)
      .forcePad.append(value)
  }

  def increment(column: String, value: String): CQLQuery = {
    counterSetter(column, CQLSyntax.Symbols.+, value)
  }

  def decrement(column: String, value: String): CQLQuery = {
    counterSetter(column, CQLSyntax.Symbols.-, value)
  }

  def set(column: String, value: String): CQLQuery = {
    Utils.concat(column, CQLSyntax.Symbols.`=`, value)
  }

  def setTo(column: String, value: String): CQLQuery = {
    Utils.concat(column, CQLSyntax.Symbols.`=`, value)
  }

  def set(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.Symbols.`=`, clause)
  }

  def andSet(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.and, clause)
  }

}

