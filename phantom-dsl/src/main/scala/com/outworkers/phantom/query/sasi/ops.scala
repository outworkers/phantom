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
package com.outworkers.phantom.builder.query.sasi

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.WhereClause
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.sasi.Mode.{Contains, Prefix}

trait SASIOp[RR] {
  def qb: CQLQuery
}

sealed class PrefixOp[RR](value: String) extends SASIOp[RR] {
  override def qb: CQLQuery = QueryBuilder.SASI.prefixValue(value)
}

sealed class SuffixOp[RR](value: String) extends SASIOp[RR] {
  override def qb: CQLQuery = QueryBuilder.SASI.suffixValue(value)
}

sealed class ContainsOp[RR](value: String) extends SASIOp[RR] {
  override def qb: CQLQuery = QueryBuilder.SASI.containsValue(value)
}

private[phantom] trait DefaultSASIOps {
  object prefix {
    def apply(value: String): PrefixOp[String] = new PrefixOp(value)

    def apply[RR : Numeric](value: RR): PrefixOp[RR] = new PrefixOp(value.toString)
  }

  object suffix {
    def apply(value: String): SuffixOp[String] = new SuffixOp(value)

    def apply[RR : Numeric](value: RR): SuffixOp[RR] = new SuffixOp(value.toString)
  }

  object contains {
    def apply(value: String): ContainsOp[String] = new ContainsOp(value)

    def apply[RR : Numeric](value: RR): ContainsOp[RR] = new ContainsOp(value.toString)
  }
}

trait AllowedSASIOp[M <: Mode, Op <: SASIOp[_]]

object AllowedSASIOp {
  implicit def modePrefixCanPrefix[T]: AllowedSASIOp[Prefix, PrefixOp[T]] = {
    new AllowedSASIOp[Mode.Prefix, PrefixOp[T]] {}
  }

  implicit def modeContainsCanPrefix[T]: AllowedSASIOp[Contains, PrefixOp[T]] = {
    new AllowedSASIOp[Mode.Contains, PrefixOp[T]] {}
  }

  implicit def modeContainsCanSuffix[T]: AllowedSASIOp[Contains, SuffixOp[T]] = {
    new AllowedSASIOp[Mode.Contains, SuffixOp[T]] {}
  }

  implicit def modeContainsCanContains[T]: AllowedSASIOp[Contains, ContainsOp[T]] = {
    new AllowedSASIOp[Mode.Contains, ContainsOp[T]] {}
  }
}

class SASITextOps[M <: Mode](
  col: String
)(implicit ev: Primitive[String]) {

  def like[Op <: SASIOp[String]](op: Op)(implicit ev: AllowedSASIOp[M, Op]): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.SASI.likeAny(col, op.qb.queryString))
  }
}
