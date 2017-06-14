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
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.dsl._

trait SASIOp {
  def qb: CQLQuery
}

sealed class PrefixOp(value: String) extends SASIOp {
  override def qb: CQLQuery = QueryBuilder.SASI.prefixValue(value)
}

sealed class SuffixOp(value: String) extends SASIOp {
  override def qb: CQLQuery = QueryBuilder.SASI.suffixValue(value)
}

sealed class ContainsOp(value: String) extends SASIOp {
  override def qb: CQLQuery = QueryBuilder.SASI.containsValue(value)
}

private[phantom] trait DefaultSASIOps {
  object prefix {
    def apply(value: String): PrefixOp = new PrefixOp(value)
  }

  object suffix {
    def apply(value: String): SuffixOp = new SuffixOp(value)
  }

  object contains {
    def apply(value: String): ContainsOp = new ContainsOp(value)
  }
}

trait AllowedSASIOp[M <: Mode, Op <: SASIOp]

object AllowedSASIOp {
  implicit val modePrefixCanPrefix = new AllowedSASIOp[Mode.Prefix, PrefixOp] {}
  implicit val modeContainsCanPrefix = new AllowedSASIOp[Mode.Contains, PrefixOp] {}
  implicit val modeSparseCanPrefix = new AllowedSASIOp[Mode.Sparse, PrefixOp] {}
  implicit val modeContainsCanSuffix = new AllowedSASIOp[Mode.Contains, SuffixOp] {}
  implicit val modeContainsCanContains = new AllowedSASIOp[Mode.Contains, ContainsOp] {}
}

class SASITextOps[M <: Mode](
  index: SASIIndex[M] with AbstractColumn[String]
)(implicit ev: Primitive[String]) {

  def like[Op <: SASIOp](op: Op)(implicit ev: AllowedSASIOp[M, Op]): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.SASI.likeAny(index.name, op.qb.queryString))
  }
}

