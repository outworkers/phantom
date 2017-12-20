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
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.builder.query.sasi.Mode.{Contains, Prefix}
import shapeless.{HList, HNil, ::}

trait SASIOp[RR, HL <: HList] {
  def qb: CQLQuery
}

sealed class PrefixOp[RR, PS <: HList](value: String) extends SASIOp[RR, PS] {
  override def qb: CQLQuery = QueryBuilder.SASI.prefixValue(value)
}

sealed class SuffixOp[RR, PS <: HList](value: String) extends SASIOp[RR, PS] {
  override def qb: CQLQuery = QueryBuilder.SASI.suffixValue(value)
}

sealed class ContainsOp[RR, PS <: HList](value: String) extends SASIOp[RR, PS] {
  override def qb: CQLQuery = QueryBuilder.SASI.containsValue(value)
}


private[phantom] trait DefaultSASIOps {
  object prefix {
    def apply(value: String): PrefixOp[String, HNil] = new PrefixOp(value)

    def apply(mark: PrepareMark): PrefixOp[String, PrefixValue :: HNil] = {
      new PrefixOp[String, PrefixValue :: HNil](mark.qb.queryString) {
        override def qb: CQLQuery = mark.qb
      }
    }

    def apply[RR : Numeric](value: RR): PrefixOp[RR, HNil] = new PrefixOp(value.toString)
  }

  object suffix {
    def apply(value: String): SuffixOp[String, HNil] = new SuffixOp(value)

    def apply(mark: PrepareMark): SuffixOp[String, SuffixValue :: HNil] = {
      new SuffixOp[String, SuffixValue :: HNil](mark.qb.queryString) {
        override def qb: CQLQuery = mark.qb
      }
    }

    def apply[RR : Numeric](value: RR): SuffixOp[RR, HNil] = new SuffixOp(value.toString)
  }

  object contains {
    def apply(value: String): ContainsOp[String, HNil] = new ContainsOp(value)

    def apply(mark: PrepareMark): ContainsOp[String, ContainsValue :: HNil] = {
      new ContainsOp[String, ContainsValue :: HNil](mark.qb.queryString) {
        override def qb: CQLQuery = mark.qb
      }
    }

    def apply[RR : Numeric](value: RR): ContainsOp[RR, HNil] = new ContainsOp(value.toString)
  }
}

class AllowedSASIOp[M <: Mode, Op <: SASIOp[_, _]]

object AllowedSASIOp {
  implicit def modePrefixCanPrefix[T, HL <: HList]: AllowedSASIOp[Prefix, PrefixOp[T, HL]] = {
    new AllowedSASIOp[Mode.Prefix, PrefixOp[T, HL]]
  }

  implicit def modeContainsCanPrefix[T, HL <: HList]: AllowedSASIOp[Contains, PrefixOp[T, HL]] = {
    new AllowedSASIOp[Mode.Contains, PrefixOp[T, HL]]
  }

  implicit def modeContainsCanSuffix[T, HL <: HList]: AllowedSASIOp[Contains, SuffixOp[T, HL]] = {
    new AllowedSASIOp[Mode.Contains, SuffixOp[T, HL]]
  }

  implicit def modeContainsCanContains[T, HL <: HList]: AllowedSASIOp[Contains, ContainsOp[T, HL]] = {
    new AllowedSASIOp[Mode.Contains, ContainsOp[T, HL]]
  }
}

class SASITextOps[M <: Mode](
  col: String
)(implicit ev: Primitive[String]) {

  def like[
    Op[R, H <: HList] <: SASIOp[R, H],
    Rec,
    HL <: HList
  ](op: Op[Rec, HL])(implicit ev: AllowedSASIOp[M, Op[Rec, HL]]): WhereClause.HListCondition[HL] = {
    new WhereClause.HListCondition[HL](QueryBuilder.SASI.likeAny(col, op.qb.queryString))
  }
}
