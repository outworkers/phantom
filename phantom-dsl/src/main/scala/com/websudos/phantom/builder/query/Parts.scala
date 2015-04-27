/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query

import com.websudos.phantom.builder.QueryBuilder

sealed abstract class QueryPart[T <: QueryPart[T]](val queryList: List[CQLQuery] = Nil) {

  def instance(l: List[CQLQuery]): T

  def qb: CQLQuery

  def build(init: CQLQuery): CQLQuery = if (init.nonEmpty) {
    qb.bpad prepend init
  } else {
    qb prepend init
  }

  def append(q: CQLQuery): T = instance(queryList ::: (q :: Nil))

  def merge[X <: QueryPart[X]](part: X): MergedQueryList = {

    val list = if (part.qb.nonEmpty) List(qb, part.qb) else List(qb)

    new MergedQueryList(list)
  }
}

sealed class MergedQueryList(val list: List[CQLQuery]) {

  def this(query: CQLQuery) = this(List(query))

  def build: CQLQuery = CQLQuery(list.map(_.queryString).mkString(" "))

  /**
   * This will build a merge list into a final executable query.
   * It will also prepend the CQL query passed as a parameter to the final string.
   *
   * If the current list has only empty queries to merge, the init string is return instead.
   * Alternatively, the init string is prepended after a single space.
   *
   * @param init The initialisation query of the part merge.
   * @return A final, executable CQL query with all the parts merged.
   */
  def build(init: CQLQuery): CQLQuery = if (list.exists(_.nonEmpty)) {
    build.bpad prepend init
  } else {
    init
  }

  def merge[X <: QueryPart[X]](part: X, init: CQLQuery = CQLQuery.empty): MergedQueryList = {

    val appendable = part build init

    if (appendable.nonEmpty) {
      new MergedQueryList(list ::: List(appendable))
    } else {
      this
    }
  }
}

sealed class UsingPart(val list: List[CQLQuery] = Nil) extends QueryPart[UsingPart](list) {

  override def qb: CQLQuery = list match {
    case head :: tail => QueryBuilder.Update.usingPart(list)
    case Nil => CQLQuery.empty
  }

  override def instance(l: List[CQLQuery]): UsingPart = new UsingPart(l)
}

sealed class WherePart(val list: List[CQLQuery] = Nil) extends QueryPart[WherePart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(list: List[CQLQuery]): WherePart = new WherePart(list)
}

sealed class LimitedPart(val list: List[CQLQuery] = Nil) extends QueryPart[LimitedPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): LimitedPart = new LimitedPart(l)
}

sealed class OrderPart(val list: List[CQLQuery] = Nil) extends QueryPart[OrderPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): OrderPart = new OrderPart(l)
}

sealed class FilteringPart(val list: List[CQLQuery] = Nil) extends QueryPart[FilteringPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): FilteringPart = new FilteringPart(l)
}

sealed class SetPart(val list: List[CQLQuery] = Nil) extends QueryPart[SetPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.chain(list)

  override def instance(l: List[CQLQuery]): SetPart = new SetPart(l)
}

sealed class CompareAndSetPart(val list: List[CQLQuery] = Nil) extends QueryPart[CompareAndSetPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): CompareAndSetPart = new CompareAndSetPart(l)
}

sealed class ColumnsPart(val list: List[CQLQuery] = Nil) extends QueryPart[ColumnsPart](list) {
  override def qb: CQLQuery = QueryBuilder.Insert.columns(list)

  override def instance(l: List[CQLQuery]): ColumnsPart = new ColumnsPart(l)
}

sealed class ValuePart(val list: List[CQLQuery] = Nil) extends QueryPart[ValuePart](list) {
  override def qb: CQLQuery = QueryBuilder.Insert.values(list)

  override def instance(l: List[CQLQuery]): ValuePart = new ValuePart(l)
}

sealed class LightweightPart(val list: List[CQLQuery] = Nil) extends QueryPart[LightweightPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): LightweightPart = new LightweightPart(l)
}


private[phantom] object Defaults {
  val EmptyUsingPart = new UsingPart()
  val EmptyWherePart = new WherePart()
  val EmptySetPart = new SetPart()
  val EmptyCompareAndSetPart = new CompareAndSetPart()
  val EmptyLimitPart = new LimitedPart()
  val EmptyOrderPart = new OrderPart()
  val EmptyFilteringPart = new FilteringPart()
  val EmptyValuePart = new ValuePart()
  val EmptyColumnsPart = new ColumnsPart()
  val EmptyLightweightPart = new LightweightPart()
}

