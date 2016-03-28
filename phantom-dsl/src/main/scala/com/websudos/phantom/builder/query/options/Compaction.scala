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
package com.websudos.phantom.builder.query.options

import com.twitter.util.StorageUnit
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

sealed abstract class CompactionStrategy(override val qb: CQLQuery) extends TablePropertyClause(qb)

private[phantom] trait CompactionStrategies {

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

private[phantom] class CompactionBuilder extends TableProperty {
  def eqs(clause: CompactionStrategy): TablePropertyClause = {
    new TablePropertyClause(QueryBuilder.Create.compaction(clause.qb))
  }
}
