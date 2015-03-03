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

import org.joda.time.Seconds

import scala.annotation.implicitNotFound

import com.twitter.util.StorageUnit
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._

import scala.concurrent.duration.FiniteDuration


sealed trait WithBound
sealed trait WithChainned extends WithBound
sealed trait WithUnchainned extends WithBound

sealed trait CompactionBound
sealed trait SpecifiedCompaction extends CompactionBound
sealed trait UnspecifiedCompaction extends CompactionBound


sealed class CreateOptionClause(val qb: CQLQuery) {}

sealed class CompactionStrategy(override val qb: CQLQuery) extends CreateOptionClause(qb)

sealed trait CompactionStrategies {

  private[this] def rootStrategy(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CompactionOptions.`class`)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(strategy)
  }

  sealed class SizeTieredCompactionStrategy(override val qb: CQLQuery) extends CompactionStrategy(qb) {
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



sealed class CompressionStrategy(override val qb: CQLQuery) extends CreateOptionClause(qb) {

  def chunk_length_kb(unit: StorageUnit): CompressionStrategy = {
    new CompressionStrategy(QueryBuilder.chunk_length_kb(qb, unit.toHuman()))
  }

  def crc_check_chance(size: Double): CompressionStrategy = {
    new CompressionStrategy(QueryBuilder.crc_check_chance(qb, size))
  }
}

sealed trait CompressionStrategies {

  private[this] def rootStrategy(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CompressionOptions.sstable_compression)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(strategy)
  }

  case object SnappyCompressor extends CompressionStrategy(rootStrategy(CQLSyntax.CompressionStrategies.SnappyCompressor))
  case object LZ4Compressor extends CompressionStrategy(rootStrategy(CQLSyntax.CompressionStrategies.LZ4Compressor))
  case object DeflateCompressor extends CompressionStrategy(rootStrategy(CQLSyntax.CompressionStrategies.DeflateCompressor))
}

/**
 * A root implementation trait of a CQL table option.
 * These are implemented with respect to the CQL 3.0 reference available here: {{ http://www.datastax.com/documentation/cql/3.0/cql/cql_reference/tabProp
 * .html }}
 */
sealed trait TableProperty

/**
 * A collection of available table property clauses with all the default objects available.
 * This serves as a helper trait for [[com.websudos.phantom.dsl._]] and brings all the relevant options into scope.
 */
trait TablePropertyClauses extends CompactionStrategies with CompressionStrategies {
  object Storage {
    case object CompactStorage extends CreateOptionClause(CQLQuery(CQLSyntax.StorageMechanisms.CompactStorage))
  }

  object compression extends TableProperty {
    def eqs(clause: CompressionStrategy): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.compression(clause.qb))
    }
  }

  object compaction extends TableProperty {
    def eqs(clause: CompactionStrategy): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.compaction(clause.qb))
    }
  }

  object comment extends TableProperty {
    def eqs(clause: String): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.comment(clause))
    }
  }

  object read_repair_chance extends TableProperty {
    def eqs(clause: Double): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.read_repair_chance(clause.toString))
    }
  }

  object dclocal_read_repair_chance extends TableProperty {
    def eqs(clause: Double): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.dclocal_read_repair_chance(clause.toString))
    }
  }

  object replicate_on_write extends TableProperty {
    def apply(clause: Boolean): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.dclocal_read_repair_chance(clause.toString))
    }

    def eqs = apply _
  }

  object gc_grace_seconds extends TableProperty {

    def eqs(clause: Seconds): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.populate_io_cache_on_flush(clause.getSeconds.toString))
    }

    def eqs(duration: FiniteDuration): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.populate_io_cache_on_flush(duration.toSeconds.toString))
    }

    def eqs(duration: com.twitter.util.Duration): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.populate_io_cache_on_flush(duration.inSeconds.toString))
    }

  }

  object bloom_filter_fp_chance extends TableProperty {
    def eqs(clause: Double): CreateOptionClause = {
      new CreateOptionClause(QueryBuilder.Create.bloom_filter_fp_chance(clause.toString))
    }
  }

}

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
  def `with`(clause: CreateOptionClause)(implicit ev: Chain =:= WithUnchainned): CreateQuery[Table, Record, Status, WithChainned] = {
    new CreateQuery(table, QueryBuilder.`with`(qb, clause.qb))
  }

  @implicitNotFound("You have to use `with` before using `and` in a create query.")
  def and(clause: CreateOptionClause)(implicit ev: Chain =:= WithChainned): CreateQuery[Table, Record, Status, WithChainned] = {
    new CreateQuery(table, QueryBuilder.and(qb, clause.qb))
  }

}

private[phantom] trait CreateImplicits extends TablePropertyClauses {
  implicit def rootCreateQueryToCreateQuery[T <: CassandraTable[T, _], R](root: RootCreateQuery[T, R]): CreateQuery[T, R, Unspecified, WithUnchainned]#Default = {
    new CreateQuery(root.table, root.default)
  }
}
