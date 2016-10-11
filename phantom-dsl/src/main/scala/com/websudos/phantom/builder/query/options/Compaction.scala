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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.{CQLQuery, OptionPart}
import com.websudos.phantom.builder.syntax.CQLSyntax

sealed abstract class CompactionProperties[
  T <: CompactionProperties[T]
](val options: OptionPart) extends ClauseBuilder[T] {

  def enabled(flag: Boolean): T = option(CQLSyntax.CompactionOptions.enabled, flag.toString)

  def tombstone_compaction_interval(interval: Long): T = {
    option(
      CQLSyntax.CompactionOptions.tombstone_compaction_interval,
      interval.toString
    )
  }

  def tombstone_threshold(value: Double): T = {
    option(
      CQLSyntax.CompactionOptions.tombstone_threshold,
      value.toString
    )
  }

  def unchecked_tombstone_compaction(value: Double): T = {
    option(
      CQLSyntax.CompactionOptions.unchecked_tombstone_compaction,
      value.toString
    )
  }
}

private[phantom] trait CompactionStrategies {

  private[this] def strategy(strategy: String): OptionPart = {
    OptionPart(
      CQLQuery.empty
      .appendSingleQuote(CQLSyntax.CompactionOptions.`class`)
      .append(CQLSyntax.Symbols.colon)
      .forcePad.appendSingleQuote(strategy)
    )
  }

  sealed class SizeTieredCompactionStrategy(options: OptionPart)
    extends CompactionProperties[SizeTieredCompactionStrategy](options) {

    def min_sstable_size(value: Int): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.min_sstable_size,
        value.toString
      )
    }

    def max_threshold(value: Int): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.max_threshold,
        value.toString
      )
    }

    def min_threshold(value: Int): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.min_threshold,
        value.toString
      )
    }

    def bucket_high(value: Double): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.bucket_high,
        value.toString
      )
    }

    def cold_reads_to_omit(value: Double): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.cold_reads_to_omit,
        value.toString
      )
    }

    def bucket_low(value: Double): SizeTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.bucket_low,
        value.toString
      )
    }

    override protected[this] def instance(options: OptionPart): SizeTieredCompactionStrategy = {
      new SizeTieredCompactionStrategy(options)
    }
  }

  sealed class LeveledCompactionStrategy(options: OptionPart)
    extends CompactionProperties[LeveledCompactionStrategy](options) {

    def sstable_size_in_mb(value: Int): LeveledCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.sstable_size_in_mb,
        value.toString
      )
    }

    override protected[this] def instance(options: OptionPart): LeveledCompactionStrategy = {
      new LeveledCompactionStrategy(options)
    }
  }

  sealed class DateTieredCompactionStrategy(options: OptionPart)
    extends CompactionProperties[DateTieredCompactionStrategy](options) {

    override protected[this] def instance(opts: OptionPart): DateTieredCompactionStrategy = {
      new DateTieredCompactionStrategy(opts)
    }

    def base_time_seconds(value: Long): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.base_time_seconds,
        value.toString
      )
    }

    def max_sstable_age_days(value: Long): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.max_sstable_age_days,
        value.toString
      )
    }

    def max_threshold(value: Int): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.max_threshold,
        value.toString
      )
    }

    def min_threshold(value: Int): DateTieredCompactionStrategy = {
      option(
        CQLSyntax.CompactionOptions.min_threshold,
        value.toString
      )
    }
  }

  case object SizeTieredCompactionStrategy extends SizeTieredCompactionStrategy(
    strategy(CQLSyntax.CompactionStrategies.sizeTiered)
  )

  case object LeveledCompactionStrategy extends LeveledCompactionStrategy(
    strategy(CQLSyntax.CompactionStrategies.leveled)
  )

  case object DateTieredCompactionStrategy extends DateTieredCompactionStrategy(
    strategy(CQLSyntax.CompactionStrategies.dateTiered)
  )
}

private[phantom] class CompactionBuilder extends TableProperty {
  def eqs(clause: CompactionProperties[_]): TablePropertyClause = {
    new TablePropertyClause {
      def qb: CQLQuery = QueryBuilder.Create.compaction(clause.qb)
    }
  }
}
