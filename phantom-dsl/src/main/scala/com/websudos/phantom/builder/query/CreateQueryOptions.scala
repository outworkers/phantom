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
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.syntax.CQLSyntax
import org.joda.time.Seconds

import scala.concurrent.duration.FiniteDuration

sealed trait WithBound
sealed trait WithChainned extends WithBound
sealed trait WithUnchainned extends WithBound

sealed trait CompactionBound
sealed trait SpecifiedCompaction extends CompactionBound
sealed trait UnspecifiedCompaction extends CompactionBound


sealed class TablePropertyClause(val qb: CQLQuery)

sealed abstract class CompactionStrategy(override val qb: CQLQuery) extends TablePropertyClause(qb)

sealed trait CompactionStrategies {

  private[this] def strategy(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CompactionOptions.`class`)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(strategy)
  }


  sealed abstract class CompactionProperties[
    T <: CompactionStrategy
  ](override val qb: CQLQuery) extends CompactionStrategy(qb) {

    protected[this] def instance(qb: CQLQuery): T

    def enabled(flag: Boolean): T = {
      instance(QueryBuilder.Create.enabled(qb, flag))
    }

    def tombstone_compaction_interval(interval: Long): T = {
      instance(QueryBuilder.Create.tombstone_compaction_interval(qb, interval.toString))
    }

    def tombstone_threshold(value: Double): T = {
      instance(QueryBuilder.Create.tombstone_threshold(qb, value))
    }

    def unchecked_tombstone_compaction(value: Double): T = {
      instance(QueryBuilder.Create.unchecked_tombstone_compaction(qb, value))
    }

  }

  sealed class SizeTieredCompactionStrategy(override val qb: CQLQuery)
    extends CompactionProperties[SizeTieredCompactionStrategy](qb) {

    def min_sstable_size(unit: StorageUnit): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(
        QueryBuilder.Create.min_sstable_size(
          qb,
          unit.inMegabytes.toString
        )
      )
    }

    def max_threshold(value: Int): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(QueryBuilder.Create.max_threshold(qb, value))
    }

    def min_threshold(value: Int): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(QueryBuilder.Create.min_threshold(qb, value))
    }

    def bucket_high(size: Double): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(QueryBuilder.Create.bucket_high(qb, size))
    }

    def cold_reads_to_omit(value: Double): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(QueryBuilder.Create.cold_reads_to_omit(qb, value))
    }

    def bucket_low(size: Double): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(QueryBuilder.Create.bucket_low(qb, size))
    }

    override protected[this] def instance(qb: CQLQuery): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(qb)
    }
  }

  sealed class LeveledCompactionStrategy(override val qb: CQLQuery)
    extends CompactionProperties[LeveledCompactionStrategy](qb) {

    def sstable_size_in_mb(unit: StorageUnit): LeveledCompactionStrategy = {
      new LeveledCompactionStrategy(
        QueryBuilder.Create.sstable_size_in_mb(qb, unit.inMegabytes.toString)
      )
    }

    override protected[this] def instance(qb: CQLQuery): LeveledCompactionStrategy = {
      new LeveledCompactionStrategy(qb)
    }
  }

  sealed class DateTieredCompactionStrategy(override val qb: CQLQuery)
    extends CompactionProperties[DateTieredCompactionStrategy](qb) {
    override protected[this] def instance(qb: CQLQuery): DateTieredCompactionStrategy = {
      new DateTieredCompactionStrategy(qb)
    }

    def base_time_seconds(value: Long): DateTieredCompactionStrategy = {
      new DateTieredCompactionStrategy(QueryBuilder.Create.base_time_seconds(qb, value))
    }

    def max_sstable_age_days(value: Long): DateTieredCompactionStrategy = {
      new DateTieredCompactionStrategy(QueryBuilder.Create.max_sstable_age_days(qb, value))
    }

    def max_threshold(value: Int): DateTieredCompactionStrategy = {
      new DateTieredCompactionStrategy(QueryBuilder.Create.max_threshold(qb, value))
    }

    def min_threshold(value: Int): DateTieredCompactionStrategy = {
      new DateTieredCompactionStrategy(QueryBuilder.Create.min_threshold(qb, value))
    }

  }

  case object SizeTieredCompactionStrategy extends SizeTieredCompactionStrategy(strategy(CQLSyntax.CompactionStrategies.SizeTieredCompactionStrategy))
  case object LeveledCompactionStrategy extends LeveledCompactionStrategy(strategy(CQLSyntax.CompactionStrategies.LeveledCompactionStrategy))
  case object DateTieredCompactionStrategy extends DateTieredCompactionStrategy(strategy(CQLSyntax.CompactionStrategies.DateTieredCompactionStrategy))
}

sealed class CompressionStrategy(override val qb: CQLQuery) extends TablePropertyClause(qb) {

  def chunk_length_kb(unit: StorageUnit): CompressionStrategy = {
    new CompressionStrategy(QueryBuilder.Create.chunk_length_kb(qb, unit.toHuman()))
  }

  def crc_check_chance(size: Double): CompressionStrategy = {
    new CompressionStrategy(QueryBuilder.Create.crc_check_chance(qb, size))
  }
}

sealed trait CompressionStrategies {

  private[this] def strategy(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CompressionOptions.sstable_compression)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(strategy)
  }

  case object SnappyCompressor extends CompressionStrategy(strategy(CQLSyntax.CompressionStrategies.SnappyCompressor))
  case object LZ4Compressor extends CompressionStrategy(strategy(CQLSyntax.CompressionStrategies.LZ4Compressor))
  case object DeflateCompressor extends CompressionStrategy(strategy(CQLSyntax.CompressionStrategies.DeflateCompressor))
}

sealed abstract class CacheProperty(override val qb: CQLQuery) extends TablePropertyClause(qb)

sealed trait CachingStrategies {
  private[this] def caching(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CacheStrategies.Caching)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(strategy)
  }

  case object None extends CacheProperty(CQLQuery(CQLSyntax.CacheStrategies.None))
  case object KeysOnly extends CacheProperty(CQLQuery(CQLSyntax.CacheStrategies.KeysOnly))
  case object RowsOnly extends CacheProperty(CQLQuery(CQLSyntax.CacheStrategies.RowsOnly))
  case object All extends CacheProperty(CQLQuery(CQLSyntax.CacheStrategies.All))
}

object Caching extends CachingStrategies

/**
  * A root implementation trait of a CQL table option.
  * These are implemented with respect to the CQL 3.0 reference available here:
  * {{ http://www.datastax.com/documentation/cql/3.0/cql/cql_reference/tabProp.html }}
  */
sealed trait TableProperty

/**
  * A collection of available table property clauses with all the default objects available.
  * This serves as a helper trait for [[com.websudos.phantom.dsl]] and brings all the relevant options into scope.
  */
private[phantom] trait TablePropertyClauses extends CompactionStrategies with CompressionStrategies {
  object Storage {
    case object CompactStorage extends TablePropertyClause(CQLQuery(CQLSyntax.StorageMechanisms.CompactStorage))
  }

  /**
  * Helper object used to specify the compression strategy for a table.
  * According to the Cassandra specification, the available strategies are:
  *
  * <ul>
  *   <li>SnappyCompressor</li>
  *   <li>LZ4Compressor</li>
  *   <li>DeflateCompressor</li>
  * </ul>
  *
  * A simple way to define a strategy is by using the {{eqs}} method.
  *
  * {{{
  *  import com.websudos.phantom.dsl._
  *
  *  SomeTable.create.with(compression eqs SnappyCompressor)
  *
  * }}}
  *
  * Using a compression strategy also allows using the API methods of controlling compressor behaviour:
  *
  * {{{
  *   import com.websudos.phantom.dsl._
  *   import com.twitter.conversions.storage._
  *
  *   SomeTable.create.`with`(compression eqs SnappyCompressor.chunk_length_kb(50.kilobytes))
  *
  * }}}
  */
  object compression extends TableProperty {
    def eqs(clause: CompressionStrategy): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.compression(clause.qb))
    }
  }

  /**
  * Table creation clause allowing specification of CQL compaction strategies.
  *
  * <ul>
  *   <li>SizeTieredCompactionStrategy</li>
  *   <li>LeveledCompactionStrategy</li>
  *   <li>DateTieredCompactionStrategy</li>
  * </ul>
  *
  * {{{
  *   import com.websudos.phantom.dsl._
  *
  *   SomeTable.create.`with`(compaction eqs SnappyCompressor)
  * }}}
  */
  object compaction extends TableProperty {
    def eqs(clause: CompactionStrategy): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.compaction(clause.qb))
    }
  }

  object comment extends TableProperty {
    def eqs(clause: String): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.comment(clause))
    }
  }

  object read_repair_chance extends TableProperty {
    def eqs(clause: Double): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.read_repair_chance(clause.toString))
    }
  }

  object dclocal_read_repair_chance extends TableProperty {
    def eqs(clause: Double): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.dclocal_read_repair_chance(clause.toString))
    }
  }

  object replicate_on_write extends TableProperty {
    def apply(clause: Boolean): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.replicate_on_write(clause.toString))
    }

    def eqs(clause: Boolean): TablePropertyClause = apply(clause)
  }

  object gc_grace_seconds extends TableProperty {

    def eqs(clause: Seconds): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.gc_grace_seconds(clause.getSeconds.toString))
    }

    def eqs(duration: FiniteDuration): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.gc_grace_seconds(duration.toSeconds.toString))
    }

    def eqs(duration: com.twitter.util.Duration): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.gc_grace_seconds(duration.inSeconds.toString))
    }
  }

  object bloom_filter_fp_chance extends TableProperty {
    def eqs(clause: Double): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.bloom_filter_fp_chance(clause.toString))
    }
  }

  object caching extends TableProperty {
    def eqs(strategy: CacheProperty): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.caching(strategy.qb.queryString))
    }
  }

  object default_time_to_live extends TableProperty {

    def eqs(time: Long): TablePropertyClause = {
      new TablePropertyClause(QueryBuilder.Create.default_time_to_live(time.toString))
    }

    def eqs(duration: Seconds): TablePropertyClause = {
      eqs(duration.getSeconds.toLong)
    }

    def eqs(duration: FiniteDuration): TablePropertyClause = {
      eqs(duration.toSeconds)
    }

    def eqs(duration: com.twitter.util.Duration): TablePropertyClause = {
      eqs(duration.inLongSeconds)
    }
  }

}