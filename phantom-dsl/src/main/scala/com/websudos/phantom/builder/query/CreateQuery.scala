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

import com.twitter.util.StorageUnit
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._

import scala.annotation.implicitNotFound

sealed trait WithBound
sealed trait WithChainned extends WithBound
sealed trait WithUnchainned extends WithBound

sealed trait CompactionBound
sealed trait SpecifiedCompaction extends CompactionBound
sealed trait UnspecifiedCompaction extends CompactionBound


sealed class WithClause(val qb: CQLQuery) {}

sealed class EscapableWithClause(override val qb: CQLQuery, escaped : Boolean = false) extends WithClause(qb) {

  private[this] def escapeIfUnescaped(): EscapableWithClause = {
    if (escaped) {
      this
    } else {
      new EscapableWithClause(QueryBuilder.escapeOptions(qb), true)
    }
  }
}

sealed class CompactionStrategy(val qb: CQLQuery)

trait WithClauses {
  object Storage {
    case object CompactStorage extends WithClause(CQLQuery(CQLSyntax.StorageMechanisms.CompactStorage))
  }

  object Compression {
    sealed class BuilderClause(val qb: CQLQuery) {

    }
  }

  object Compaction {

    private[this] def rootStrategy(strategy: String) = {
      CQLQuery.empty.append(CQLSyntax.CompactionOptions.`class`)
        .forcePad.append(CQLSyntax.Symbols.`:`)
        .append(strategy)
    }

    sealed class SizeTieredCompactionStrategy(qb: CQLQuery) extends WithClause(qb) {
      def min_sstable_size(unit: StorageUnit): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.min_sstable_size(qb, unit.toHuman()))
      }

      def sstable_size_in_mb(unit: StorageUnit): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.sstable_size_in_mb(qb, unit.toHuman()))
      }

      def bucket_high(size: Double): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.bucket_high(qb, size))
      }

      def bucket_low(size: Double): SizeTieredCompactionStrategy = {
        new SizeTieredCompactionStrategy(QueryBuilder.bucket_low(qb, size))
      }
    }


    case object SizeTieredCompactionStrategy extends SizeTieredCompactionStrategy(rootStrategy(CQLSyntax.CompactionStrategies.SizeTieredCompactionStrategy))
  }
}

object WithClauses extends WithClauses

class RootCreateQuery[
  Table <: CassandraTable[Table, _],
  Record
](val table: Table, val qb: CQLQuery) {

  private[phantom] def default: CQLQuery = {
    CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.table)
      .forcePad.append(table.tableName).forcePad
      .append(CQLSyntax.Symbols.`(`)
      .append(QueryBuilder.join(table.columns.map(_.qb): _*))
      .append(CQLSyntax.Symbols.`,`)
      .forcePad.append(table.defineTableKey())
      .append(CQLSyntax.Symbols.`)`)
  }

  def ifNotExists: CQLQuery = {
    CQLQuery(CQLSyntax.create)
  }
}


class CreateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  Chain <: WithBound
](table: Table, val qb: CQLQuery) extends ExecutableStatement {

  type Default = CreateQuery[Table, Record, Unspecified, WithUnchainned]

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  def `with`(clause: WithClause)(implicit ev: Chain =:= WithUnchainned): CreateQuery[Table, Record, Status, WithChainned] = {
    new CreateQuery(table, QueryBuilder.`with`(qb, clause.qb))
  }

  @implicitNotFound("You have to use `with` before using `and` in a create query.")
  def and(clause: WithClause)(implicit ev: Chain =:= WithChainned): CreateQuery[Table, Record, Status, WithChainned] = {
    new CreateQuery(table, QueryBuilder.and(qb, clause.qb))
  }

  def compaction(strategy: CompactionStrategy): CreateQuery[Table, Record, Status, WithChainned] = {
    new CreateQuery(table, QueryBuilder.withCompaction(qb, strategy.qb))
  }

}

private[phantom] trait CreateImplicits {
  implicit def rootCreateQueryToCreateQuery[T <: CassandraTable[T, _], R](root: RootCreateQuery[T, R]): CreateQuery[T, R, Unspecified, WithUnchainned]#Default = {
    new CreateQuery(root.table, root.default)
  }
}
