package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[builder] trait Utils {

  def concat(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.forcePad.append(clause)
  }

  def concat(qb: CQLQuery, op: String, clause: CQLQuery): CQLQuery = {
    qb.pad.append(op).forcePad.append(clause)
  }

  def operator(op: String, clause: CQLQuery): CQLQuery = {
    CQLQuery(op).forcePad.append(clause)
  }

  def concat(column: String, op: String, value: String) = {
    CQLQuery(column).pad.append(op).forcePad.append(value)
  }

  def join(list: TraversableOnce[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`(`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`)`)
  }

  def join(qbs: CQLQuery*): CQLQuery = {
    CQLQuery(qbs.map(_.queryString).mkString(", "))
  }

  def collection(list: TraversableOnce[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`[`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`]`)
  }

  def set(list: Set[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`}`)
  }

  def map(list: TraversableOnce[(String, String)]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`)
      .append(list.map(item => {s"${item._1} : ${item._2}"}).mkString(", "))
      .append(CQLSyntax.Symbols.`}`)
  }
}

private[builder] object Utils extends Utils
