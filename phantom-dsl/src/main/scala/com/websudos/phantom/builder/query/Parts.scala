package com.websudos.phantom.builder.query

import com.websudos.phantom.builder.QueryBuilder

sealed abstract class QueryPart[T <: QueryPart[T]](val queryList: List[CQLQuery] = Nil) {

  def instance(l: List[CQLQuery]): T

  def qb: CQLQuery

  def build: CQLQuery = CQLQuery(queryList.map(_.queryString).mkString(" "))

  def build(init: CQLQuery): CQLQuery = build prepend " " prepend init

  def append(q: CQLQuery): T = instance(q :: queryList)

  def merge[X <: QueryPart[X]](part: X): X = part.instance(queryList ::: part.queryList)
}

sealed class UsingPart(val l: List[CQLQuery] = Nil) extends QueryPart[UsingPart](l) {

  override def qb: CQLQuery = l match {
    case head :: tail => QueryBuilder.Update.usingPart(l)
    case Nil => CQLQuery.empty
  }

  override def instance(l: List[CQLQuery]): UsingPart = new UsingPart(l)
}

sealed class WherePart(val l: List[CQLQuery] = Nil) extends QueryPart[WherePart](l) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(l)

  override def instance(l: List[CQLQuery]): WherePart = new WherePart(l)
}

sealed class SetPart(val l: List[CQLQuery] = Nil) extends QueryPart[SetPart](l) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(l)

  override def instance(l: List[CQLQuery]): SetPart = new SetPart(l)
}

sealed class CompareAndSetPart(val list: List[CQLQuery] = Nil) extends QueryPart[CompareAndSetPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): CompareAndSetPart = new CompareAndSetPart(l)
}

object Defaults {
  case object EmptyUsingPart extends UsingPart(Nil)
  case object EmptyWherePart extends WherePart(Nil)
  case object EmptySetPart extends SetPart(Nil)
  case object EmptyCompareAndSetPart extends CompareAndSetPart(Nil)
}

