/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.query

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.{CQLQuery, MergeList, QueryPart}
import com.outworkers.phantom.builder.syntax.CQLSyntax

sealed abstract class CQLQueryPart[Part <: CQLQueryPart[Part]](
  override val list: List[CQLQuery]
) extends QueryPart[Part](list) {
  override def mergeList(list: List[CQLQuery]): MergeList = new MergeList(list)
}

sealed class UsingPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[UsingPart](list) {

  override def qb: CQLQuery = list match {
    case head :: tail => QueryBuilder.Update.usingPart(list)
    case Nil => CQLQuery.empty
  }

  override def instance(l: List[CQLQuery]): UsingPart = new UsingPart(l)
}

object UsingPart {
  def empty: UsingPart = new UsingPart()
}

sealed class WherePart(
  override val list: List[CQLQuery] = Nil
) extends CQLQueryPart[WherePart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(list: List[CQLQuery]): WherePart = new WherePart(list)
}

object WherePart {
  def empty: WherePart = new WherePart(Nil)
}

sealed class LimitedPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[LimitedPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): LimitedPart = new LimitedPart(l)
}

object LimitedPart {
  def empty: LimitedPart = new LimitedPart(Nil)
}

sealed class OrderPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[OrderPart](list) {
  override def qb: CQLQuery = list match {
    case head :: tail => QueryBuilder.Select.Ordering.orderBy(list: _*)
    case Nil => CQLQuery.empty
  }

  override def instance(l: List[CQLQuery]): OrderPart = new OrderPart(l)
}

object OrderPart {
  def empty: OrderPart = new OrderPart(Nil)
}

sealed class FilteringPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[FilteringPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): FilteringPart = new FilteringPart(l)
}

object FilteringPart {
  def empty: FilteringPart = new FilteringPart()
}

sealed class SetPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[SetPart](list) {

  def appendConditionally(qb: CQLQuery, flag: Boolean): SetPart = {
    if (flag) {
      append(qb)
    } else {
      this
    }
  }

  override def qb: CQLQuery = list match {
    case Nil => CQLQuery.empty
    case head :: tail => QueryBuilder.Update.set(QueryBuilder.Update.chain(list))
  }

  override def instance(l: List[CQLQuery]): SetPart = new SetPart(l)
}

object SetPart {
  def empty: SetPart = new SetPart()
}

sealed class CompareAndSetPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[CompareAndSetPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): CompareAndSetPart = new CompareAndSetPart(l)
}

object CompareAndSetPart {
  def empty: CompareAndSetPart = new CompareAndSetPart()
}

sealed class ColumnsPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[ColumnsPart](list) {
  override def qb: CQLQuery = QueryBuilder.Insert.columns(list)

  override def instance(l: List[CQLQuery]): ColumnsPart = new ColumnsPart(l)
}

object ColumnsPart {
  def empty: ColumnsPart = new ColumnsPart()
}

sealed class ValuePart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[ValuePart](list) {
  override def qb: CQLQuery = QueryBuilder.Insert.values(list)

  override def instance(l: List[CQLQuery]): ValuePart = new ValuePart(l)
}

object ValuePart {
  def empty: ValuePart = new ValuePart()
}

sealed class LightweightPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[LightweightPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): LightweightPart = new LightweightPart(l)
}

object LightweightPart {
  def empty: LightweightPart = new LightweightPart()
}

sealed class WithPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[WithPart](list) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(list)

  override def instance(l: List[CQLQuery]): WithPart = new WithPart(l)
}

object WithPart {
  def empty: WithPart = new WithPart()
}

sealed class OptionPart(override val list: List[CQLQuery] = Nil) extends CQLQueryPart[OptionPart](list) {
  override def qb: CQLQuery = QueryBuilder.Utils.options(list)

  override def instance(l: List[CQLQuery]): OptionPart = new OptionPart(l)

  def option(key: String, value: String): OptionPart = {
    val qb = QueryBuilder.Utils.option(
      CQLQuery.escape(key),
      CQLSyntax.Symbols.colon,
      value
    )

    this.append(qb)
  }
}

object OptionPart {

  def apply(qb: CQLQuery): OptionPart = new OptionPart(qb :: Nil)

  def empty: OptionPart = new OptionPart()
}
