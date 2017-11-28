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
  override val queries: Seq[CQLQuery]
) extends QueryPart[Part](queries) {

  override def mergeList(list: Seq[CQLQuery]): MergeList = new MergeList(list)
}

sealed class UsingPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[UsingPart](queries) {

  override def qb: CQLQuery = queries match {
    case head :: tail => QueryBuilder.Update.usingPart(queries)
    case Nil => CQLQuery.empty
  }

  override def instance(l: Seq[CQLQuery]): UsingPart = new UsingPart(l)
}

object UsingPart {
  def empty: UsingPart = new UsingPart()
}

sealed class WherePart(
  override val queries: Seq[CQLQuery] = Seq.empty
) extends CQLQueryPart[WherePart](queries) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(queries)

  override def instance(list: Seq[CQLQuery]): WherePart = new WherePart(list)
}

object WherePart {
  def empty: WherePart = new WherePart(Nil)
}

sealed class LimitedPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[LimitedPart](queries) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(queries)

  override def instance(l: Seq[CQLQuery]): LimitedPart = new LimitedPart(l)
}

object LimitedPart {
  def empty: LimitedPart = new LimitedPart(Nil)
}

sealed class OrderPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[OrderPart](queries) {
  override def qb: CQLQuery = queries match {
    case Seq() => CQLQuery.empty
    case _ => QueryBuilder.Select.Ordering.orderBy(queries)
  }

  override def instance(l: Seq[CQLQuery]): OrderPart = new OrderPart(l)
}

object OrderPart {
  def empty: OrderPart = new OrderPart(Nil)
}

sealed class FilteringPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[FilteringPart](queries) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(queries)

  override def instance(l: Seq[CQLQuery]): FilteringPart = new FilteringPart(l)
}

object FilteringPart {
  def empty: FilteringPart = new FilteringPart()
}

sealed class SetPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[SetPart](queries) {

  def appendConditionally(qb: CQLQuery, flag: Boolean): SetPart = {
    if (flag) {
      append(qb)
    } else {
      this
    }
  }

  override def qb: CQLQuery = queries match {
    case Seq() => CQLQuery.empty
    case _ => QueryBuilder.Update.set(QueryBuilder.Update.chain(queries))
  }

  override def instance(l: Seq[CQLQuery]): SetPart = new SetPart(l)
}

object SetPart {
  def empty: SetPart = new SetPart()
}

sealed class CompareAndSetPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[CompareAndSetPart](queries) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(queries)

  override def instance(l: Seq[CQLQuery]): CompareAndSetPart = new CompareAndSetPart(l)
}

object CompareAndSetPart {
  def empty: CompareAndSetPart = new CompareAndSetPart()
}

sealed class ColumnsPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[ColumnsPart](queries) {
  override def qb: CQLQuery = QueryBuilder.Insert.columns(queries)

  override def instance(l: Seq[CQLQuery]): ColumnsPart = new ColumnsPart(l)
}

object ColumnsPart {
  def empty: ColumnsPart = new ColumnsPart()
}

sealed class ValuePart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[ValuePart](queries) {
  override def qb: CQLQuery = QueryBuilder.Insert.values(queries)

  override def instance(l: Seq[CQLQuery]): ValuePart = new ValuePart(l)
}

object ValuePart {
  def empty: ValuePart = new ValuePart()
}

sealed class LightweightPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[LightweightPart](queries) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(queries)

  override def instance(l: Seq[CQLQuery]): LightweightPart = new LightweightPart(l)
}

object LightweightPart {
  def empty: LightweightPart = new LightweightPart()
}

sealed class WithPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[WithPart](queries) {
  override def qb: CQLQuery = QueryBuilder.Update.clauses(queries)

  override def instance(l: Seq[CQLQuery]): WithPart = new WithPart(l)
}

object WithPart {
  def empty: WithPart = new WithPart()
}

sealed class OptionPart(override val queries: Seq[CQLQuery] = Seq.empty) extends CQLQueryPart[OptionPart](queries) {
  override def qb: CQLQuery = QueryBuilder.Utils.options(queries)

  override def instance(l: Seq[CQLQuery]): OptionPart = new OptionPart(l)

  def option(key: String, value: String): OptionPart = {
    append {
      QueryBuilder.Utils.option(
        CQLQuery.escape(key),
        CQLSyntax.Symbols.colon,
        value
      )
    }
  }

  def option(key: String, value: Boolean): OptionPart = {
    option(key, CQLQuery.escape(value.toString))
  }
}

object OptionPart {

  def apply(qb: CQLQuery): OptionPart = new OptionPart(qb :: Nil)

  def empty: OptionPart = new OptionPart()
}
