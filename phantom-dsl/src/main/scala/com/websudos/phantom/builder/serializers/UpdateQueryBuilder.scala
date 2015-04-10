package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[builder] class UpdateQueryBuilder {

  def `if`(column: String, value: String): CQLQuery = {
    Utils.concat(column, CQLSyntax.Operators.eqs, value)
  }

  def onlyIf(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb.pad.append(CQLSyntax.`if`), clause)
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

  def set(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.set).forcePad.append(clause)
  }

  def andSet(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.and).forcePad.append(clause)
  }

  def where(condition: CQLQuery): CQLQuery = {
   Utils.operator(CQLSyntax.where, condition)
  }

  def and(condition: CQLQuery): CQLQuery = {
    Utils.operator(CQLSyntax.and, condition)
  }

  def clauses(clauses: List[CQLQuery]) = {
    CQLQuery.empty.append(clauses.map(_.queryString).mkString(" "))
  }

  def clauses(op: String, clauses: List[CQLQuery]) = {
    CQLQuery(op)
      .forcePad
      .append(clauses.map(_.queryString).mkString(" "))
  }

  def usingPart(queries: List[CQLQuery]): CQLQuery = {
    clauses(CQLSyntax.using, queries)
  }


}

